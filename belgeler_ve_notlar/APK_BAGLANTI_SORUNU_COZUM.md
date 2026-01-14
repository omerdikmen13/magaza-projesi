# 🔧 APK Bağlantı Sorunu Çözüm Planı

## ❌ Sorun:
APK kuruldu ama:
- Giriş başarısız
- Mağaza bulunamadı

## 🔍 Muhtemel Sebep:
Mobil uygulama **AWS EC2 sunucusuna bağlanamıyor**. 

---

## ✅ KONTROL LİSTESİ (Sırayla Yap)

### 1️⃣ EC2 Sunucusu Çalışıyor mu?
AWS Console'dan kontrol et:
- EC2 instance'ın **Running** durumunda mı?
- Public IP adresi nedir? (Şu an kodda: `13.60.76.224`)

### 2️⃣ Spring Boot Backend Çalışıyor mu?
EC2'ye SSH ile bağlan ve kontrol et:
```bash
ssh -i key.pem ec2-user@13.60.76.224
ps aux | grep java
```
Eğer çalışmıyorsa başlat:
```bash
cd /home/ec2-user/magaza-projesi
./mvnw spring-boot:run &
```

### 3️⃣ Security Group Portu Açık mı?
AWS Console → EC2 → Security Groups:
- **Inbound Rules** içinde **8080** portu **0.0.0.0/0** için açık mı?

### 4️⃣ Backend'e Erişim Testi
Tarayıcıdan veya terminalden test et:
```
http://13.60.76.224:8080/api/urunler
```
Bu URL çalışıyorsa backend aktif demektir.

### 5️⃣ Mobil URL Doğru mu?
`magaza-mobil/services/apiClient.ts` dosyasında:
```typescript
const BASE_URL = 'http://13.60.76.224:8080';  // Bu IP doğru mu?
```

---

## 🛠️ Döndüğünde Yapacaklarımız:

1. EC2 sunucusunun durumunu kontrol et
2. Spring Boot'u EC2'de başlat
3. API'yi test et
4. Gerekirse APK'yı yeniden build et

---

**Namazın kabul olsun! Döndüğünde bu planı uygularız.** 🤲
