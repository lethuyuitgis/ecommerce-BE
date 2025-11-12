#!/bin/bash

# Script để tạo bucket trong MinIO

echo "📦 Creating bucket 'shopcuathuy' in MinIO..."

# Đợi MinIO sẵn sàng
echo "⏳ Waiting for MinIO to be ready..."
sleep 5

# Tạo bucket bằng MinIO client (mc)
if command -v mc &> /dev/null; then
    # Configure MinIO client
    mc alias set local http://localhost:9000 minioadmin minioadmin
    
    # Tạo bucket
    mc mb local/shopcuathuy --ignore-existing
    
    # Set bucket policy để public read (cho phép đọc công khai)
    mc anonymous set download local/shopcuathuy
    
    if [ $? -eq 0 ]; then
        echo "✅ Bucket 'shopcuathuy' created successfully with public read access"
    else
        echo "❌ Failed to create bucket"
        echo "   Please create it manually at http://localhost:9001"
    fi
else
    echo "⚠️  MinIO client (mc) not found"
    echo "   Please create bucket manually:"
    echo "   1. Open http://localhost:9001"
    echo "   2. Login with minioadmin/minioadmin"
    echo "   3. Click 'Create Bucket'"
    echo "   4. Name: shopcuathuy"
    echo ""
    echo "   Or install MinIO client:"
    echo "   brew install minio/stable/minio"
fi






