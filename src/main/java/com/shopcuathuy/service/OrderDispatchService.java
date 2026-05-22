package com.shopcuathuy.service;

import com.shopcuathuy.entity.Order;
import com.shopcuathuy.entity.Shipment;
import com.shopcuathuy.entity.ShippingMethod;
import com.shopcuathuy.entity.ShippingPartner;
import com.shopcuathuy.entity.User;
import com.shopcuathuy.entity.UserAddress;
import com.shopcuathuy.repository.OrderRepository;
import com.shopcuathuy.repository.ShipmentRepository;
import com.shopcuathuy.repository.ShippingMethodRepository;
import com.shopcuathuy.repository.ShippingPartnerRepository;
import com.shopcuathuy.repository.UserAddressRepository;
import com.shopcuathuy.repository.UserRepository;
import com.shopcuathuy.repository.OrderTimelineRepository;
import com.shopcuathuy.entity.OrderTimeline;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
public class OrderDispatchService {

    private static final Logger log = LoggerFactory.getLogger(OrderDispatchService.class);
    private final OrderRepository orderRepository;
    private final ShipmentRepository shipmentRepository;
    private final ShippingMethodRepository shippingMethodRepository;
    private final ShippingPartnerRepository shippingPartnerRepository;
    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;
    private final OrderTimelineRepository orderTimelineRepository;
    private final ShippingService shippingService;
    private final Random random = new Random();

    @Autowired
    public OrderDispatchService(
            OrderRepository orderRepository,
            ShipmentRepository shipmentRepository,
            ShippingMethodRepository shippingMethodRepository,
            ShippingPartnerRepository shippingPartnerRepository,
            UserRepository userRepository,
            UserAddressRepository userAddressRepository,
            OrderTimelineRepository orderTimelineRepository,
            ShippingService shippingService) {
        this.orderRepository = orderRepository;
        this.shipmentRepository = shipmentRepository;
        this.shippingMethodRepository = shippingMethodRepository;
        this.shippingPartnerRepository = shippingPartnerRepository;
        this.userRepository = userRepository;
        this.userAddressRepository = userAddressRepository;
        this.orderTimelineRepository = orderTimelineRepository;
        this.shippingService = shippingService;
    }

    /**
     * Tự động điều phối đơn hàng cho shipper sau khi đặt hàng thành công
     */
    @Transactional
    public void dispatchOrder(String orderId) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

            // Kiểm tra xem đã có shipment chưa
            if (shipmentRepository.findByOrderId(orderId).isPresent()) {
                log.info("Order {} already has a shipment, skipping dispatch", orderId);
                return;
            }

            // 1. Create shipment record
            Shipment shipment = createShipmentForOrder(order);

            // 2. Set order to CONFIRMED and PENDING (awaiting pickup)
            order.setShippingStatus(Order.ShippingStatus.PENDING);
            order.setStatus(Order.OrderStatus.CONFIRMED);
            orderRepository.save(order);
            addTimelineEntry(order, "CONFIRMED", "Đơn hàng đã được xác nhận và đang chờ shipper nhận hàng.", "system");

            // 3. Attempt to assign a shipper (this will update status further if successful)
            assignToShipper(shipment);

            log.info("Order {} processed for dispatch", orderId);
        } catch (Exception e) {
            log.error("Error dispatching order {}: {}", orderId, e.getMessage(), e);
            // Không throw exception để không làm gián đoạn quá trình tạo đơn
        }
    }

    /**
     * Tạo shipment cho đơn hàng
     */
    private Shipment createShipmentForOrder(Order order) {
        Shipment shipment = new Shipment();
        shipment.setId(UUID.randomUUID().toString());
        shipment.setOrder(order);

        // Lấy shipping method mặc định hoặc đầu tiên (chỉ lấy active)
        List<ShippingMethod> methods = shippingMethodRepository.findByIsActiveTrue();
        if (!methods.isEmpty()) {
            shipment.setShippingMethod(methods.get(0));
        }

        // Tạo tracking number
        shipment.setTrackingNumber(generateTrackingNumber(order));

        // Use Order snapshot information (Correct way)
        shipment.setRecipientName(order.getRecipientName());
        shipment.setRecipientPhone(order.getRecipientPhone());
        shipment.setRecipientAddress(order.getRecipientAddress());
        shipment.setRecipientProvince(order.getRecipientProvince());
        shipment.setRecipientDistrict(order.getRecipientDistrict());
        shipment.setRecipientWard(order.getRecipientWard());

        // Lấy địa chỉ lấy hàng từ seller
        if (order.getSeller() != null && order.getSeller().getUser() != null) {
            User sellerUser = order.getSeller().getUser();
            shipment.setSenderName(order.getSeller().getShopName());
            shipment.setSenderPhone(sellerUser.getPhone());
            // Có thể lấy địa chỉ seller từ Seller entity nếu có
        }

        // Tính toán phí vận chuyển
        shipment.setShippingFee(order.getShippingFee());
        shipment.setCodAmount(order.getPaymentMethod() != null && order.getPaymentMethod().equals("cod") 
                ? order.getFinalTotal() 
                : null);

        // Trạng thái ban đầu
        shipment.setStatus(Shipment.ShipmentStatus.READY_FOR_PICKUP);
        shipment.setExpectedDeliveryDate(LocalDate.now().plusDays(3)); // Dự kiến giao trong 3 ngày

        Shipment saved = shipmentRepository.save(shipment);
        
        // Initial tracking
        shippingService.addTrackingUpdate(saved.getId(), "READY_FOR_PICKUP", "Warehouse", "Đơn hàng đã sẵn sàng được lấy.");
        
        return saved;
    }

    /**
     * Điều phối shipment cho shipper
     */
    private void assignToShipper(Shipment shipment) {
        // Tìm tất cả shipper đang hoạt động
        List<User> shippers = userRepository.findByUserType(User.UserType.SHIPPER);
        
        // Lọc chỉ lấy shipper đang ACTIVE và được APPROVED
        List<User> activeShippers = shippers.stream()
                .filter(shipper -> (shipper.getStatus() == null || shipper.getStatus() == User.UserStatus.ACTIVE) && 
                                   (shipper.getApprovalStatus() == User.ApprovalStatus.APPROVED))
                .toList();

        if (activeShippers.isEmpty()) {
            log.warn("No approved shippers found, shipment {} will remain unassigned", shipment.getId());
            return;
        }

        // Chọn shipper (có thể cải thiện logic: shipper có ít đơn nhất)
        User selectedShipper = activeShippers.get(random.nextInt(activeShippers.size()));

        shipment.setShipper(selectedShipper);
        shipment.setStatus(Shipment.ShipmentStatus.PICKED_UP);
        shipmentRepository.save(shipment);

        // Add to timeline for order
        addTimelineEntry(shipment.getOrder(), "PICKED_UP", "Đơn hàng đã được shipper " + selectedShipper.getFullName() + " nhận lấy hàng.", "system");

        log.info("Shipment {} assigned to shipper user {}", shipment.getId(), selectedShipper.getId());
    }

    /**
     * Tìm hoặc tạo ShippingPartner cho shipper user
     */
    private ShippingPartner findOrCreateShippingPartnerForShipper(User shipper) {
        // Tìm xem đã có ShippingPartner cho shipper này chưa (dựa vào partnerCode = shipper userId)
        return shippingPartnerRepository.findAll().stream()
                .filter(p -> p.getPartnerCode() != null && p.getPartnerCode().equals("SHIPPER_" + shipper.getId()))
                .findFirst()
                .orElseGet(() -> {
                    // Tạo mới ShippingPartner cho shipper
                    ShippingPartner partner = new ShippingPartner();
                    partner.setId(UUID.randomUUID().toString());
                    partner.setPartnerName(shipper.getFullName() + " - Shipper");
                    partner.setPartnerCode("SHIPPER_" + shipper.getId());
                    partner.setIsActive(true);
                    partner.setContactEmail(shipper.getEmail());
                    partner.setContactPhone(shipper.getPhone());
                    return shippingPartnerRepository.save(partner);
                });
    }

    /**
     * Tạo tracking number
     */
    private String generateTrackingNumber(Order order) {
        return "TRK" + order.getOrderNumber().substring(3) + System.currentTimeMillis() % 10000;
    }

    private void addTimelineEntry(Order order, String status, String note, String createdBy) {
        OrderTimeline timeline = new OrderTimeline();
        timeline.setId(UUID.randomUUID().toString());
        timeline.setOrder(order);
        timeline.setStatus(status);
        timeline.setNote(note);
        timeline.setCreatedBy(createdBy);
        orderTimelineRepository.save(timeline);
    }
}

