#!/bin/bash
# AWS EC2 İlk Kurulum Scripti
# Bu scripti EC2 instance'a ilk kez bağlandığınızda çalıştırın

echo "🚀 AWS EC2 Deployment Setup Başlıyor..."

# Sistem güncellemesi
echo "📦 Sistem güncelleniyor..."
sudo apt update && sudo apt upgrade -y

# Docker kurulumu
echo "🐳 Docker kuruluyor..."
sudo apt install -y docker.io docker-compose
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker $USER

# Git kurulumu
echo "📥 Git kuruluyor..."
sudo apt install -y git

# Nginx kurulumu
echo "🌐 Nginx kuruluyor..."
sudo apt install -y nginx

# Projeyi klonla
echo "📂 Proje klonlanıyor..."
cd /home/ubuntu
git clone https://github.com/YOUR_USERNAME/magaza-sistemi.git
cd magaza-sistemi

# .env dosyasını oluştur
echo "⚙️ Environment variables ayarlanıyor..."
cat > .env << 'EOL'
MYSQL_ROOT_PASSWORD=your_mysql_password_here
GMAIL_USER=kaptandikmen@gmail.com
GMAIL_APP_PASSWORD=pzpifokphzyekbtr
AWS_ACCESS_KEY_ID=your_aws_key
AWS_SECRET_ACCESS_KEY=your_aws_secret
AWS_S3_BUCKET_NAME=magazaapp2026
AWS_S3_REGION=eu-north-1
GEMINI_API_KEY=your_gemini_key
EOL

echo "🔒 .env dosyasını düzenlemeyi unutmayın!"
echo "nano .env"

# Docker container'ları başlat
echo "🚀 Docker container'lar başlatılıyor..."
docker-compose up -d

echo ""
echo "✅ Kurulum tamamlandı!"
echo ""
echo "📋 Sonraki adımlar:"
echo "1. nano .env - Environment variables'ı düzenle"
echo "2. docker-compose restart - Container'ları yeniden başlat"
echo "3. docker-compose logs -f - Logları izle"
echo ""
echo "🌐 Uygulamaya erişim:"
echo "http://$(curl -s ifconfig.me):8080"
