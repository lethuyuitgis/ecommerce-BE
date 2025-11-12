# Hướng dẫn Set Bucket Policy Public Read trong MinIO

## Cách 1: Qua MinIO Console (Web UI)

1. **Mở MinIO Console**: http://localhost:9001
2. **Login**: minioadmin / minioadmin
3. **Click vào bucket `shopcuathuy`** (không phải vào folder products)
4. **Ở phía trên bên phải**, tìm các tab hoặc menu:
   - Có thể có tab **"Access Policy"** hoặc **"Policy"**
   - Hoặc icon **⚙️ Settings** hoặc **🔒 Policy**
   - Hoặc menu dropdown **"..."** (3 chấm) bên cạnh bucket name
5. **Chọn Access Policy**:
   - Chọn **"Public"** hoặc **"Download"** (cho phép đọc công khai)
   - Hoặc chọn **"Custom Policy"** và paste policy JSON bên dưới

## Cách 2: Qua Command Line (mc client)

### Nếu đã cài MinIO Client (mc):

```bash
# Configure MinIO client
mc alias set local http://localhost:9000 minioadmin minioadmin

# Set bucket policy to public read
mc anonymous set download local/shopcuathuy

# Hoặc set full public access (read + write)
mc anonymous set public local/shopcuathuy
```

### Nếu chưa cài mc:

**macOS:**
```bash
brew install minio/stable/minio
```

**Linux:**
```bash
wget https://dl.min.io/client/mc/release/linux-amd64/mc
chmod +x mc
sudo mv mc /usr/local/bin/
```

**Windows:**
Download từ: https://dl.min.io/client/mc/release/windows-amd64/mc.exe

## Cách 3: Set Policy JSON trực tiếp

Nếu MinIO Console có option "Custom Policy", paste JSON này:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "AWS": ["*"]
      },
      "Action": ["s3:GetObject"],
      "Resource": ["arn:aws:s3:::shopcuathuy/*"]
    }
  ]
}
```

## Cách 4: Sửa trong code (tạm thời)

Nếu không tìm thấy Settings, có thể dùng direct URL thay vì presigned URL. Sửa `MinIOService.getFileUrl()`:

```java
public String getFileUrl(String objectName) {
    // Dùng direct URL (cần bucket public)
    return minIOConfig.getEndpoint() + "/" + minIOConfig.getBucketName() + "/" + objectName;
}
```

Sau đó set bucket public qua command line hoặc UI.

## Kiểm tra

Sau khi set public, test URL:
```
http://localhost:9000/shopcuathuy/products/your-image.webp
```

Nếu không còn lỗi "Access Denied" là thành công!


