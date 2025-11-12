# Hướng dẫn chạy MinIO

## Cách 1: Sử dụng Docker (Khuyến nghị)

### 1. Kiểm tra Docker đang chạy

```bash
docker info
```

Nếu lỗi "Cannot connect to Docker daemon", bạn cần start Docker:
- **macOS**: Mở Docker Desktop hoặc chạy `colima start`
- **Linux**: `sudo systemctl start docker`

### 2. Chạy MinIO

```bash
cd e-commerce-backend
./run-minio.sh
```

Hoặc chạy thủ công:

```bash
# Tạo thư mục data
mkdir -p minio-data

# Chạy MinIO container
docker run -d \
  --name minio \
  -p 9000:9000 \
  -p 9001:9001 \
  -e "MINIO_ROOT_USER=minioadmin" \
  -e "MINIO_ROOT_PASSWORD=minioadmin" \
  -v "$(pwd)/minio-data:/data" \
  minio/minio server /data --console-address ":9001"
```

### 3. Tạo bucket

```bash
./create-bucket.sh
```

Hoặc tạo thủ công:
1. Mở http://localhost:9001
2. Login với `minioadmin` / `minioadmin`
3. Click "Create Bucket"
4. Tên bucket: `shopcuathuy`

## Cách 2: Download và chạy MinIO binary

### 1. Download MinIO

```bash
# macOS
wget https://dl.min.io/server/minio/release/darwin-amd64/minio
chmod +x minio

# Hoặc dùng Homebrew
brew install minio/stable/minio
```

### 2. Chạy MinIO

```bash
# Tạo thư mục data
mkdir -p minio-data

# Chạy MinIO
export MINIO_ROOT_USER=minioadmin
export MINIO_ROOT_PASSWORD=minioadmin
./minio server minio-data --console-address ":9001"
```

## Kiểm tra MinIO đang chạy

### 1. Kiểm tra container

```bash
docker ps | grep minio
```

### 2. Kiểm tra API

```bash
curl http://localhost:9000/minio/health/live
```

Nếu trả về `200 OK` thì MinIO đang chạy.

### 3. Truy cập Console

Mở browser: http://localhost:9001

## Thông tin đăng nhập

- **Console URL**: http://localhost:9001
- **API URL**: http://localhost:9000
- **Username**: `minioadmin`
- **Password**: `minioadmin`

## Cấu hình trong application.yml

```yaml
minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket-name: shopcuathuy
  secure: false
```

## Troubleshooting

### Lỗi: "Cannot connect to Docker daemon"

**Giải pháp:**
- macOS: Mở Docker Desktop hoặc chạy `colima start`
- Linux: `sudo systemctl start docker`

### Lỗi: "Port 9000 already in use"

**Giải pháp:**
```bash
# Kiểm tra process đang dùng port 9000
lsof -i :9000

# Dừng process hoặc đổi port
docker run -p 9002:9000 -p 9003:9001 ...
```

### Lỗi: "Bucket not found"

**Giải pháp:**
1. Mở http://localhost:9001
2. Login với minioadmin/minioadmin
3. Tạo bucket tên `shopcuathuy`

### Kiểm tra bucket đã tạo

```bash
# Sử dụng MinIO client
mc alias set local http://localhost:9000 minioadmin minioadmin
mc ls local

# Hoặc qua browser
# Mở http://localhost:9001 và xem danh sách buckets
```

## Dừng MinIO

```bash
# Dừng container
docker stop minio

# Xóa container (giữ data)
docker rm minio

# Xóa container và data
docker rm -v minio
```

## Xem logs

```bash
docker logs minio
docker logs -f minio  # Follow logs
```







