#!/bin/bash
###############################################
# AWS EC2 Ilk Kurulum Scripti
# Spring Boot + Python Email Microservice
###############################################

echo "=========================================="
echo "🚀 AWS EC2 Full Stack Deployment Setup"
echo "=========================================="

# Sistem guncelleme
echo ""
echo "📦 Sistem guncelleniyor..."
sudo apt update && sudo apt upgrade -y

# Python ve pip
echo ""
echo "🐍 Python kuruluyor..."
sudo apt install -y python3 python3-pip python3-venv

# Java 17
echo ""
echo "☕ Java 17 kuruluyor..."
sudo apt install -y openjdk-17-jre-headless

# Git
echo ""
echo "📥 Git kuruluyor..."
sudo apt install -y git

# Proje klasorleri olustur
echo ""
echo "📁 Klasorler olusturuluyor..."
mkdir -p ~/email-microservice
mkdir -p ~/logs

# .env dosyasi olustur (email microservice icin)
echo ""
echo "⚙️ Environment variables ayarlaniyor..."
cat > ~/email-microservice/.env << 'EOF'
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USER=kaptandikmen@gmail.com
MAIL_PASS=pzpifokphzyekbtr
EOF

echo ""
echo "=========================================="
echo "✅ Kurulum tamamlandi!"
echo "=========================================="
echo ""
echo "📋 Sonraki adimlar:"
echo ""
echo "1. GitHub'dan projeyi cek:"
echo "   git clone https://github.com/omerdikmen13/magaza-projesi.git"
echo ""
echo "2. Python bagimliliklarini kur:"
echo "   cd ~/email-microservice"
echo "   pip3 install -r requirements.txt"
echo ""
echo "3. Python Email Microservice baslat:"
echo "   nohup python3 -m uvicorn main:app --host 0.0.0.0 --port 8000 > ~/logs/email.log 2>&1 &"
echo ""
echo "4. Spring Boot JAR'i calistir:"
echo "   nohup java -jar ~/app.jar --server.port=8080 > ~/logs/springboot.log 2>&1 &"
echo ""
echo "5. Servisleri kontrol et:"
echo "   curl http://localhost:8000"
echo "   curl http://localhost:8080"
echo ""
echo "🌐 Erisim:"
echo "   Python Email: http://$(curl -s ifconfig.me):8000"
echo "   Spring Boot:  http://$(curl -s ifconfig.me):8080"
echo ""
