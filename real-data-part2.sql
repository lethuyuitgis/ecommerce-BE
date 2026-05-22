USE shopcuathuy;
SET NAMES 'utf8mb4';

-- ===================== PRODUCTS =====================
INSERT INTO products (id, seller_id, category_id, name, description, sku, price, compare_price, quantity, status, rating, total_reviews, total_sold, total_views, is_featured, created_at, updated_at) VALUES

-- Điện thoại
('pd-iphone15pm', 's-001','cat-dienthoai','iPhone 15 Pro Max 256GB',
 'iPhone 15 Pro Max với chip A17 Pro mạnh mẽ, camera 48MP ProRAW, khung titanium cao cấp, Dynamic Island thông minh. Màn hình Super Retina XDR 6.7 inch ProMotion 120Hz. Pin 4422mAh sạc nhanh 27W.5G tốc độ cao.',
 'IP15PM-256',29990000,32990000,50,'ACTIVE',4.9,245,1280,8900,TRUE,NOW(),NOW()),

('pd-samsung-s24u','s-001','cat-dienthoai','Samsung Galaxy S24 Ultra 256GB',
 'Samsung Galaxy S24 Ultra tích hợp bút S Pen, camera 200MP zoom 10x quang học, màn hình Dynamic AMOLED 6.8 inch 120Hz, chip Snapdragon 8 Gen 3, RAM 12GB. Thay thế hoàn hảo cho Galaxy Note.',
 'SS-S24U-256',26990000,29990000,45,'ACTIVE',4.8,187,950,7200,TRUE,NOW(),NOW()),

('pd-xiaomi14u','s-001','cat-dienthoai','Xiaomi 14 Ultra 512GB',
 'Xiaomi 14 Ultra với hệ thống camera Leica Summilux, cảm biến Sony LYT-900 to nhất thế giới, chip Snapdragon 8 Gen 3, màn hình LTPO AMOLED 6.73 inch 120Hz. Sạc nhanh 90W + sạc không dây 80W.',
 'XIAO14U-512',22990000,25990000,30,'ACTIVE',4.7,132,620,5500,TRUE,NOW(),NOW()),

('pd-iphone15','s-001','cat-dienthoai','iPhone 15 128GB',
 'iPhone 15 thiết kế nhôm hiện đại, Dynamic Island, camera 48MP, chip A16 Bionic, cổng USB-C tiện lợi. Màn hình Super Retina XDR 6.1 inch. Hỗ trợ 5G.',
 'IP15-128',19990000,22990000,80,'ACTIVE',4.8,312,2100,12000,TRUE,NOW(),NOW()),

('pd-oppo-f25','s-001','cat-dienthoai','OPPO Find X7 Pro 256GB',
 'OPPO Find X7 Pro camera Hasselblad chuyên nghiệp, chip Dimensity 9300 mạnh mẽ, sạc siêu nhanh 100W, sạc không dây 50W. Màn hình AMOLED 6.82 inch 120Hz cong tràn.',
 'OPPO-FX7P-256',18990000,21990000,35,'ACTIVE',4.6,98,480,4200,FALSE,NOW(),NOW()),

-- Laptop
('pd-macbook-pro14','s-001','cat-laptop','MacBook Pro 14 inch M3 Pro',
 'MacBook Pro 14 inch chip M3 Pro 11-core CPU 14-core GPU, 18GB RAM, 512GB SSD. Màn hình Liquid Retina XDR 14.2 inch, pin lên đến 18 giờ. Kết nối Thunderbolt 4, HDMI, SD card. Phù hợp sáng tạo nội dung chuyên nghiệp.',
 'MBP14-M3P-512',52990000,57990000,20,'ACTIVE',4.9,89,280,6700,TRUE,NOW(),NOW()),

('pd-dell-xps15','s-001','cat-laptop','Dell XPS 15 9530 - Intel Core i7',
 'Dell XPS 15 Core i7-13700H, RAM 16GB DDR5, SSD 512GB, card đồ họa NVIDIA RTX 4060 8GB. Màn hình OLED 3.5K 120Hz 15.6 inch. Thiết kế mỏng nhẹ đẳng cấp. Bảo hành ProSupport 1 năm.',
 'DELL-XPS15-I7',42990000,47990000,15,'ACTIVE',4.7,67,190,4800,TRUE,NOW(),NOW()),

('pd-asus-zenbook','s-001','cat-laptop','ASUS ZenBook 14 OLED - Ryzen 7',
 'ASUS ZenBook 14 AMD Ryzen 7 7745HX, RAM 16GB, SSD 512GB NVMe, màn hình OLED 14 inch 2.8K 90Hz. Trọng lượng chỉ 1.39kg, pin 75WHr siêu bền. Bàn phím backlit, cảm ứng vân tay.',
 'ASUS-ZB14-R7',22990000,26990000,25,'ACTIVE',4.6,78,350,5200,FALSE,NOW(),NOW()),

-- Thời trang nam
('pd-polo-lacoste','s-002','cat-thoitrangnam','Áo Polo Lacoste L.12.12 Classic Fit',
 'Áo polo Lacoste chính hãng chất liệu piqué cotton 100% cao cấp. May đo chuẩn Classic Fit, cổ bẻ 3 nút, logo cá sấu thêu tay trên ngực. Wash 40°C, phù hợp cả đi chơi lẫn đi làm. Size S-XL nhiều màu.',
 'LACOSTE-POLO-001',1890000,2200000,120,'ACTIVE',4.8,156,780,5400,TRUE,NOW(),NOW()),

('pd-quan-jean-levis','s-002','cat-thoitrangnam','Quần Jean Levi''s 511 Slim Fit',
 'Quần jean Levi''s 511 Slim Fit chính hãng nhập Mỹ. Chất liệu denim cotton 99% co giãn nhẹ, form slim ôm gọn nhưng thoải mái. Màu indigo cổ điển, wash 5 túi chuẩn. Size 29-36.',
 'LEVIS-511-IND',1490000,1790000,150,'ACTIVE',4.7,203,1050,7800,TRUE,NOW(),NOW()),

('pd-giay-nike-af1','s-002','cat-thoitrangnam','Nike Air Force 1 ''07 Low White',
 'Nike Air Force 1 chính hãng Nike Việt Nam. Đế Air-Sole đệm êm, upper leather cao cấp trắng tinh. Outsole cao su bền. Thiết kế classic không bao giờ lỗi mốt. Size 38-45 nam nữ đều đi được.',
 'NIKE-AF1-WHT',2190000,2590000,200,'ACTIVE',4.9,342,1890,13500,TRUE,NOW(),NOW()),

-- Thời trang nữ
('pd-dam-zara','s-002','cat-thoitrangnu','Đầm Maxi Hoa Boho Zara Style',
 'Đầm maxi hoa boho phong cách Zara chất liệu vải lụa mềm mại nhẹ nhàng. Thiết kế váy dài đến mắt cá chân, cổ V sexy, thân áo buộc dây lưng. Phù hợp đi biển, dạo phố, chụp ảnh.',
 'DAM-MAXI-BOHO',590000,790000,300,'ACTIVE',4.6,289,1540,11200,TRUE,NOW(),NOW()),

('pd-tui-coach','s-002','cat-thoitrangnu','Túi Xách Coach Tabby 26 Chính Hãng',
 'Túi Coach Tabby 26 chính hãng Mỹ, da Pebble cao cấp, khóa C logo signature. Ngăn chính rộng chứa được A4, có ngăn kéo bên trong. Dây đeo vai tháo rời có thể đeo chéo. Kích thước 26x19x8cm.',
 'COACH-TABBY26',5990000,7200000,40,'ACTIVE',4.8,124,390,8900,TRUE,NOW(),NOW()),

-- Gia dụng
('pd-noicom-zojirushi','s-003','cat-giadung','Nồi Cơm Điện Zojirushi NL-GAQ10',
 'Nồi cơm điện Zojirushi 1 lít NL-GAQ10 công nghệ nấu cơm IH 3D cảm ứng nhiệt. Giữ ấm 12 tiếng, hẹn giờ linh hoạt. Nồi trong teflon chống dính bền, dễ rửa. Bảo hành 12 tháng chính hãng.',
 'ZOJIRU-NLGAQ10',2990000,3490000,60,'ACTIVE',4.9,178,720,6200,TRUE,NOW(),NOW()),

('pd-maylocnuoc-karofi','s-003','cat-giadung','Máy Lọc Nước Karofi KAQ-U95',
 'Máy lọc nước RO Karofi KAQ-U95 9 cấp lọc, công suất 15 lít/giờ, có đèn UV diệt khuẩn. Màn hình LED hiển thị chất lượng nước, TDS < 10ppm. Bồn chứa 10 lít. Lắp đặt miễn phí tại HCM và HN.',
 'KAROFI-KAQ-U95',3890000,4590000,35,'ACTIVE',4.7,92,380,3800,FALSE,NOW(),NOW()),

('pd-robot-huttbui','s-003','cat-giadung','Robot Hút Bụi Xiaomi Robot Vacuum S20',
 'Robot hút bụi lau nhà Xiaomi Robot Vacuum S20 lực hút 5000Pa mạnh nhất phân khúc. Lập bản đồ LiDAR chính xác, điều khiển app Mihome. Pin 5200mAh chạy 120 phút. Phù hợp nhà đến 200m2.',
 'XIAOMI-VACUUM-S20',6490000,7990000,28,'ACTIVE',4.8,134,420,5900,FALSE,NOW(),NOW()),

-- Mỹ phẩm / Làm đẹp
('pd-serum-skii','s-004','cat-mytham','SK-II Facial Treatment Essence 230ml',
 'SK-II Facial Treatment Essence nước thần huyền thoại với 90% Pitera™ - chiết xuất lên men độc quyền. Cân bằng độ ẩm, mờ vết thâm, căng bóng da chỉ sau 4 tuần. Phù hợp mọi loại da kể cả da nhạy cảm.',
 'SKII-FTE-230',3290000,3690000,80,'ACTIVE',4.9,267,1340,14500,TRUE,NOW(),NOW()),

('pd-kem-laneige','s-004','cat-mytham','Laneige Water Sleeping Mask 70ml',
 'Mặt nạ ngủ Laneige Water Sleeping Mask dưỡng ẩm tối đa qua đêm với Sleep-tox™ và công nghệ Moisture Wrap. Da sáng mịn, căng ẩm khi thức dậy. Mùi hương thư giãn lavender nhẹ nhàng. Best seller Korea.',
 'LANEIGE-WSM-70',790000,990000,150,'ACTIVE',4.8,389,2100,18000,TRUE,NOW(),NOW()),

('pd-serum-vitamin-c','s-004','cat-mytham','Paula''s Choice C15 Super Booster 30ml',
 'Serum Vitamin C 15% Paula''s Choice kết hợp Vitamin E và Ferulic Acid bảo vệ da khỏi gốc tự do. Làm sáng đều màu da, mờ nám tàn nhang, kích thích collagen. Không mùi, không kích ứng da.',
 'PC-C15-30ML',1490000,1790000,90,'ACTIVE',4.7,198,870,9800,FALSE,NOW(),NOW()),

('pd-thoi-mac','s-004','cat-mytham','Son MAC Retro Matte Lipstick Ruby Woo',
 'Son MAC Ruby Woo màu đỏ cổ điển iconic nhất thế giới, finish Retro Matte mịn lì lâu trôi cả ngày. Công thức dưỡng ẩm môi, không khô. Hộp đựng kim loại sang trọng. Không thử dùng, giữ hộp nguyên.',
 'MAC-MATTE-RUBY',890000,990000,200,'ACTIVE',4.8,312,1620,15600,TRUE,NOW(),NOW());

-- Cập nhật total_products cho sellers
UPDATE sellers SET total_products = (SELECT COUNT(*) FROM products WHERE seller_id = sellers.id AND status = 'ACTIVE');
