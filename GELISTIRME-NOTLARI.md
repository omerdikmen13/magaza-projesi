# 🚀 MagazaApp - Geliştirme Notları

## ✅ Tamamlanan Özellikler

### AI Alışveriş Asistanı
- Gemini API entegrasyonu
- Veritabanından ürün/mağaza/kategori çekme
- Akıllı link sistemi:
  - 🏪 Mağaza linki (yeşil)
  - 📂 Kategori linki (turuncu)
  - 🔗 Ürün linki (mor)

---

## 📦 GitHub'a Yükleme

### İlk Kez Yükleme
```bash
cd c:\Users\MONSTER\Desktop\magaza-projesi

# Git başlat
git init

# Tüm dosyaları ekle
git add .

# İlk commit
git commit -m "Initial commit: MagazaApp E-Ticaret Projesi"

# GitHub repo bağla (kendi repo URL'ini yaz)
git remote add origin https://github.com/KULLANICI_ADIN/magaza-projesi.git

# Yükle
git push -u origin main
```

### Değişiklik Yaptıktan Sonra
```bash
# Değişiklikleri gör
git status

# Tüm değişiklikleri ekle
git add .

# Commit yap (açıklama yaz)
git commit -m "AI chat widget güncellendi"

# GitHub'a yükle
git push
```

---

## ☁️ AWS'e Yükleme (EC2)

### 1. EC2 Instance Oluştur
- AWS Console → EC2 → Launch Instance
- Amazon Linux 2 veya Ubuntu seç
- t2.micro (Free Tier)
- Security Group: 8080 ve 22 portlarını aç

### 2. SSH ile Bağlan
```bash
ssh -i "anahtar.pem" ec2-user@EC2_IP_ADRESI
```

### 3. Java ve Maven Kur
```bash
sudo yum install java-17-amazon-corretto -y
sudo yum install maven -y
```

### 4. Projeyi Klonla ve Çalıştır
```bash
git clone https://github.com/KULLANICI_ADIN/magaza-projesi.git
cd magaza-projesi
./mvnw spring-boot:run
```

### 5. Arka Planda Çalıştır
```bash
nohup ./mvnw spring-boot:run > app.log 2>&1 &
```

---

## 🔮 Gelecek Geliştirmeler

### AI Asistanı
- [ ] Sesli arama desteği
- [ ] Görsel ürün arama (fotoğraf yükle, benzer bul)
- [ ] Kişiselleştirilmiş öneriler (kullanıcı geçmişine göre)
- [ ] Sohbet geçmişi kaydetme

### Mobil Uygulama (React Native)
- [ ] AI chat widget mobilde
- [ ] Push notification
- [ ] Offline mod
- [ ] Barkod okuyucu

### Backend
- [ ] Redis cache
- [ ] Elasticsearch ile gelişmiş arama
- [ ] API rate limiting
- [ ] WebSocket ile gerçek zamanlı bildirimler

### Güvenlik
- [ ] API key'i environment variable'a taşı
- [ ] Rate limiting ekle
- [ ] Input validation güçlendir

---

## 📁 Önemli Dosyalar

| Dosya | Açıklama |
|-------|----------|
| `AIController.java` | AI API endpoint'leri |
| `GeminiService.java` | Gemini API bağlantısı |
| `ai-chat-widget.html` | Chat widget UI |
| `application.properties` | API key ve DB ayarları |
| `.gitignore` | Git'e dahil edilmeyecek dosyalar |

---

## ⚠️ Dikkat Edilecekler

1. **API Key Güvenliği**: `application.properties` içindeki Gemini API key'ini GitHub'a yüklemeden önce çıkar veya environment variable kullan.

2. **Database**: AWS'de MySQL veya RDS kullan, local H2 yerine.

3. **CORS**: Mobil uygulama için CORS ayarlarını kontrol et.

---

*Son güncelleme: 3 Ocak 2026*
