-- =============================================================================
-- Cập nhật ảnh primary cho 20 sản phẩm:
-- 1) URLs brand-official đã verify HTTP 200 (Apple, Nike, Amazon)
-- 2) Còn lại: placehold.co với tên sản phẩm (ảnh labelled, render được, dễ phân biệt)
-- Lý do không dùng Shopee CDN trực tiếp: Shopee chặn bot scraping ở mọi tầng
-- (page HTML, API v4, image search indexing), không match được product → image hash.
-- =============================================================================

-- Apple official CDN (verified working)
UPDATE product_images SET image_url='https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/iphone-15-pro-finish-select-202309-6-7inch-bluetitanium?wid=2048&hei=1152&fmt=p-jpg' WHERE product_id='pd-iphone15pm' AND is_primary=TRUE;
UPDATE product_images SET image_url='https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/iphone-15-finish-select-202309-6-1inch-pink?wid=2048&hei=1152&fmt=p-jpg' WHERE product_id='pd-iphone15' AND is_primary=TRUE;
UPDATE product_images SET image_url='https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/mbp14-spacegray-select-202310?wid=2048&hei=1152&fmt=p-jpg' WHERE product_id='pd-macbook-pro14' AND is_primary=TRUE;

-- Nike official CDN (verified)
UPDATE product_images SET image_url='https://static.nike.com/a/images/t_PDP_1280_v1/f_auto,q_auto:eco/3396ee3c-08cc-4ada-baa9-655af12e3120/air-force-1-07-shoes-Wr0Q19.png' WHERE product_id='pd-giay-nike-af1' AND is_primary=TRUE;

-- Amazon (verified)
UPDATE product_images SET image_url='https://m.media-amazon.com/images/I/71jG+e7roXL._AC_SX679_.jpg' WHERE product_id='pd-noicom-zojirushi' AND is_primary=TRUE;

-- Placeholder cho các sản phẩm không tìm được URL ổn định
UPDATE product_images SET image_url='https://placehold.co/600x600/1e3a8a/ffffff?text=Samsung+S24+Ultra' WHERE product_id='pd-samsung-s24u' AND is_primary=TRUE;
UPDATE product_images SET image_url='https://placehold.co/600x600/ea580c/ffffff?text=Xiaomi+14+Ultra' WHERE product_id='pd-xiaomi14u' AND is_primary=TRUE;
UPDATE product_images SET image_url='https://placehold.co/600x600/16a34a/ffffff?text=OPPO+Find+X7+Pro' WHERE product_id='pd-oppo-f25' AND is_primary=TRUE;
UPDATE product_images SET image_url='https://placehold.co/600x600/0c4a6e/ffffff?text=Dell+XPS+15' WHERE product_id='pd-dell-xps15' AND is_primary=TRUE;
UPDATE product_images SET image_url='https://placehold.co/600x600/7c3aed/ffffff?text=ASUS+ZenBook' WHERE product_id='pd-asus-zenbook' AND is_primary=TRUE;
UPDATE product_images SET image_url='https://placehold.co/600x600/064e3b/ffffff?text=Polo+Lacoste' WHERE product_id='pd-polo-lacoste' AND is_primary=TRUE;
UPDATE product_images SET image_url='https://placehold.co/600x600/1e40af/ffffff?text=Levis+511+Slim' WHERE product_id='pd-quan-jean-levis' AND is_primary=TRUE;
UPDATE product_images SET image_url='https://placehold.co/600x600/9d174d/ffffff?text=Zara+Boho+Dress' WHERE product_id='pd-dam-zara' AND is_primary=TRUE;
UPDATE product_images SET image_url='https://placehold.co/600x600/78350f/ffffff?text=Coach+Tabby+26' WHERE product_id='pd-tui-coach' AND is_primary=TRUE;
UPDATE product_images SET image_url='https://placehold.co/600x600/0f766e/ffffff?text=Karofi+KAQ+U95' WHERE product_id='pd-maylocnuoc-karofi' AND is_primary=TRUE;
UPDATE product_images SET image_url='https://placehold.co/600x600/525252/ffffff?text=Xiaomi+Robot+S20' WHERE product_id='pd-robot-huttbui' AND is_primary=TRUE;
UPDATE product_images SET image_url='https://placehold.co/600x600/991b1b/ffffff?text=SK+II+Essence' WHERE product_id='pd-serum-skii' AND is_primary=TRUE;
UPDATE product_images SET image_url='https://placehold.co/600x600/be185d/ffffff?text=Laneige+Sleeping+Mask' WHERE product_id='pd-kem-laneige' AND is_primary=TRUE;
UPDATE product_images SET image_url='https://placehold.co/600x600/c2410c/ffffff?text=Paulas+Choice+C15' WHERE product_id='pd-serum-vitamin-c' AND is_primary=TRUE;
UPDATE product_images SET image_url='https://placehold.co/600x600/7f1d1d/ffffff?text=MAC+Ruby+Woo' WHERE product_id='pd-thoi-mac' AND is_primary=TRUE;
