#!/bin/bash

# Script để set bucket shopcuathuy thành public read

echo "🔓 Setting bucket 'shopcuathuy' to public read..."

# Kiểm tra xem mc có được cài đặt không
if ! command -v mc &> /dev/null; then
    echo "❌ MinIO client (mc) not found"
    echo ""
    echo "📥 Cài đặt MinIO client:"
    echo ""
    echo "macOS:"
    echo "  brew install minio/stable/minio"
    echo ""
    echo "Linux:"
    echo "  wget https://dl.min.io/client/mc/release/linux-amd64/mc"
    echo "  chmod +x mc"
    echo "  sudo mv mc /usr/local/bin/"
    echo ""
    echo "Sau khi cài, chạy lại script này."
    exit 1
fi

# Configure MinIO client
echo "⚙️  Configuring MinIO client..."
mc alias set local http://localhost:9000 minioadmin minioadmin

if [ $? -ne 0 ]; then
    echo "❌ Failed to configure MinIO client"
    echo "   Đảm bảo MinIO đang chạy tại http://localhost:9000"
    exit 1
fi

# Set bucket policy to public read (download)
echo "🔓 Setting bucket policy to public read..."
mc anonymous set download local/shopcuathuy

if [ $? -eq 0 ]; then
    echo "✅ Bucket 'shopcuathuy' đã được set thành public read!"
    echo ""
    echo "📝 Bây giờ bạn có thể:"
    echo "   1. Dùng direct URL: http://localhost:9000/shopcuathuy/products/xxx.webp"
    echo "   2. Hoặc tiếp tục dùng presigned URL (7 ngày)"
    echo ""
    echo "🧪 Test URL:"
    echo "   http://localhost:9000/shopcuathuy/products/"
else
    echo "❌ Failed to set bucket policy"
    echo ""
    echo "💡 Thử cách khác:"
    echo "   1. Mở MinIO Console: http://localhost:9001"
    echo "   2. Login: minioadmin / minioadmin"
    echo "   3. Click vào bucket 'shopcuathuy'"
    echo "   4. Tìm tab 'Access Policy' hoặc icon Settings"
    echo "   5. Chọn 'Public' hoặc 'Download'"
fi


