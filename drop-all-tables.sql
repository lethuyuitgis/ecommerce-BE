-- Script to drop all tables in the database
-- This will delete ALL data, use with caution!
-- Run this BEFORE restarting Spring Boot application

USE shopcuathuy;

-- Disable foreign key checks temporarily
SET FOREIGN_KEY_CHECKS = 0;

-- Drop all tables in reverse dependency order
DROP TABLE IF EXISTS tracking_updates;
DROP TABLE IF EXISTS shipments;
DROP TABLE IF EXISTS order_timeline;
DROP TABLE IF EXISTS payment_transactions;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS review_images;
DROP TABLE IF EXISTS product_reviews;
DROP TABLE IF EXISTS promotion_items;
DROP TABLE IF EXISTS promotions;
DROP TABLE IF EXISTS voucher_usages;
DROP TABLE IF EXISTS vouchers;
DROP TABLE IF EXISTS wishlist;
DROP TABLE IF EXISTS cart_items;
DROP TABLE IF EXISTS product_images;
DROP TABLE IF EXISTS product_variants;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS user_addresses;
DROP TABLE IF EXISTS sellers;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS shipping_methods;
DROP TABLE IF EXISTS shipping_partners;
DROP TABLE IF EXISTS payment_methods;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- After running this script, restart your Spring Boot application
-- Hibernate will automatically create all tables with correct data types (CHAR(36))
