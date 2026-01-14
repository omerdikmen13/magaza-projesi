# 📱 Expo Go'dan APK'ya Dönüştürme ve AWS Bağlantısı Planı

## ✅ KISA CEVAP
Evet, Expo uygulamasını APK'ya dönüştürebilirsin ve AWS EC2'deki backend'e bağlanabilir. **Bozulmaz**, sadece birkaç ayar yapman gerekiyor.

---

## 🔍 EXPO GO vs APK FARKI

| Özellik | Expo Go | APK |
|---------|---------|-----|
| Kullanım | Sadece geliştirme/test | Dağıtım, telefonlara yükleme |
| İnternet | Expo sunucusuna bağlı | Bağımsız çalışır |
| Backend | Localhost veya tunnel | Gerçek sunucu IP'si |
| Kurulum | App Store'dan Expo Go indir | APK dosyasını telefonlara kur |

---

## 📋 ADIM ADIM PLAN

### ADIM 1: Backend URL'ini Güncelle ✏️
**Dosya:** `magaza-mobil/services/apiClient.ts`

```typescript
// GELİŞTİRME (şu anki hali - ngrok veya localhost)
const BASE_URL = 'http://ngrok-tunnel.app:8080';

// ÜRETİM (APK için değiştir)
const BASE_URL = 'http://13.60.76.224:8080';  // ← EC2 IP adresin
```

> ⚠️ **ÖNEMLİ:** EC2'de Spring Boot'un **8080 portunda** çalışıyor olması lazım.
> AWS Security Group'ta 8080 portu açık olmalı.

---

### ADIM 2: app.json Kontrolü ✅
**Dosya:** `magaza-mobil/app.json`

```json
{
  "expo": {
    "name": "Mağaza Sistemi",
    "slug": "magaza-mobil",
    "version": "1.0.0",
    "orientation": "portrait",
    "icon": "./assets/icon.png",
    "splash": {
      "image": "./assets/splash.png",
      "resizeMode": "contain",
      "backgroundColor": "#667eea"
    },
    "android": {
      "package": "com.magazasistemi.app",
      "adaptiveIcon": {
        "foregroundImage": "./assets/adaptive-icon.png",
        "backgroundColor": "#667eea"
      }
    }
  }
}
```

---

### ADIM 3: EAS Build Kurulumu 🔧

**Terminal komutları (magaza-mobil klasöründe):**

```bash
# 1. EAS CLI'ı global olarak kur
npm install -g eas-cli

# 2. Expo hesabına giriş yap (hesabın yoksa expo.dev'den ücretsiz aç)
eas login

# 3. Projeyi EAS'a bağla
eas build:configure
```

---

### ADIM 4: eas.json Oluştur 📄
**Dosya:** `magaza-mobil/eas.json` (otomatik oluşur, kontrol et)

```json
{
  "cli": {
    "version": ">= 5.0.0"
  },
  "build": {
    "development": {
      "developmentClient": true,
      "distribution": "internal"
    },
    "preview": {
      "distribution": "internal",
      "android": {
        "buildType": "apk"
      }
    },
    "production": {
      "android": {
        "buildType": "apk"
      }
    }
  }
}
```

---

### ADIM 5: APK Build Et 🚀

```bash
# Preview APK oluştur (test için)
eas build --platform android --profile preview
```

**Ne olur?**
1. Expo sunucuları projeyi alır
2. Bulutta build eder (5-15 dakika sürer)
3. Sana indirme linki verir
4. APK'yı indir ve telefona kur

---

### ADIM 6: APK'yı Telefona Kur 📲

1. Build tamamlanınca Expo'dan **APK indirme linki** gelir
2. Linki telefonunda aç ve APK'yı indir
3. "Bilinmeyen kaynaklardan yükleme" izni ver
4. APK'yı kur
5. Uygulamayı aç ve test et!

---

## ⚠️ OLASI SORUNLAR VE ÇÖZÜMLERİ

### Sorun 1: "Network Error" / Bağlantı Hatası
**Sebep:** EC2'deki backend'e ulaşılamıyor.
**Çözüm:**
- EC2 Security Group'ta 8080 portu açık mı kontrol et
- Spring Boot gerçekten çalışıyor mu kontrol et: `curl http://EC2_IP:8080/api/urunler`
- EC2'de firewall kapalı mı: `sudo systemctl status firewalld`

### Sorun 2: "401 Unauthorized"
**Sebep:** Token geçersiz
**Çözüm:** Uygulamadan çıkış yap, tekrar giriş yap

### Sorun 3: HTTP güvensiz bağlantı hatası
**Sebep:** Android 9+ varsayılan olarak HTTP'yi engelliyor
**Çözüm:** `app.json`'a ekle:
```json
"android": {
  "usesCleartextTraffic": true
}
```

### Sorun 4: Build başarısız
**Sebep:** Genellikle bağımlılık sorunu
**Çözüm:**
```bash
# Temizle ve yeniden kur
rm -rf node_modules
npm install
eas build --platform android --profile preview
```

---

## 🎯 HIZLI KONTROL LİSTESİ

- [ ] `apiClient.ts`'de BASE_URL = EC2 IP adresi
- [ ] EC2 Security Group'ta 8080 açık
- [ ] Spring Boot EC2'de çalışıyor
- [ ] `app.json`'da `usesCleartextTraffic: true`
- [ ] EAS CLI kurulu (`npm install -g eas-cli`)
- [ ] Expo hesabına giriş yapıldı (`eas login`)
- [ ] `eas build --platform android --profile preview` çalıştırıldı
- [ ] APK indirip telefona kuruldu

---

## 💻 TAM KOMUT SIRASI

```bash
# 1. magaza-mobil klasörüne git
cd magaza-mobil

# 2. Bağımlılıkları kur
npm install

# 3. EAS CLI kur
npm install -g eas-cli

# 4. Expo'ya giriş yap
eas login

# 5. Projeyi yapılandır
eas build:configure

# 6. APK build et
eas build --platform android --profile preview

# 7. Link gelince APK'yı indir ve telefona kur
```

---

## 📊 ZAMAN TAHMİNİ

| Adım | Süre |
|------|------|
| Backend URL güncelleme | 1 dakika |
| EAS kurulum | 2-3 dakika |
| Build (Expo sunucularında) | 10-20 dakika |
| APK indirme | 1-2 dakika |
| Telefona kurma | 2 dakika |
| **TOPLAM** | **~30 dakika** |

---

## ✅ SONUÇ

1. **Bozulmaz** - APK aynı kodla çalışır
2. **AWS'e bağlanır** - Sadece BASE_URL'i EC2 IP'sine çevir
3. **Ücretsiz** - EAS'ın ücretsiz tier'ı ayda 30 build veriyor
4. **Kolay** - Sadece birkaç terminal komutu

**Başlamak için hazır mısın?** 🚀
