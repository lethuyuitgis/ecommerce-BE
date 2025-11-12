#!/bin/bash

# Script để chạy MinIO bằng Docker

echo "🚀 Starting MinIO..."

# Kiểm tra Docker daemon
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker daemon is not running. Please start Docker first."
    echo "   On macOS, you can start Docker Desktop or run: colima start"
    exit 1
fi

# Kiểm tra container đã tồn tại chưa
if docker ps -a | grep -q minio; then
    echo "📦 MinIO container already exists"
    
    # Kiểm tra container đang chạy chưa
    if docker ps | grep -q minio; then
        echo "✅ MinIO is already running"
        echo "   Console: http://localhost:9001"
        echo "   API: http://localhost:9000"
        echo "   Username: minioadmin"
        echo "   Password: minioadmin"
    else
        echo "🔄 Starting existing MinIO container..."
        docker start minio
        echo "✅ MinIO started"
        echo "   Console: http://localhost:9001"
        echo "   API: http://localhost:9000"
    fi
else
    echo "📦 Creating new MinIO container..."
    
    # Tạo thư mục để lưu data
    mkdir -p ./minio-data
    
    # Chạy MinIO container
    docker run -d \
        --name minio \
        -p 9000:9000 \
        -p 9001:9001 \
        -e "MINIO_ROOT_USER=minioadmin" \
        -e "MINIO_ROOT_PASSWORD=minioadmin" \
        -v "$(pwd)/minio-data:/data" \
        minio/minio server /data --console-address ":9001"
    
    if [ $? -eq 0 ]; then
        echo "✅ MinIO container created and started"
        echo ""
        echo "📋 MinIO Information:"
        echo "   Console: http://localhost:9001"
        echo "   API: http://localhost:9000"
        echo "   Username: minioadmin"
        echo "   Password: minioadmin"
        echo ""
        echo "📝 Next steps:"
        echo "   1. Open http://localhost:9001 in your browser"
        echo "   2. Login with minioadmin/minioadmin"
        echo "   3. Create a bucket named 'shopcuathuy'"
        echo ""
        echo "   Or wait a few seconds and run: ./create-bucket.sh"
    else
        echo "❌ Failed to create MinIO container"
        exit 1
    fi
fi







