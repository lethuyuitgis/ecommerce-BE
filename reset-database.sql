-- Script to reset database
-- Run this in MySQL to drop and recreate the database
-- This will delete ALL data, use with caution!

DROP DATABASE IF EXISTS shopcuathuy;
CREATE DATABASE shopcuathuy CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE shopcuathuy;

-- After running this script, restart your Spring Boot application
-- Hibernate will automatically create all tables with correct data types

