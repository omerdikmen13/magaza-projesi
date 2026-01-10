# 🛒 MagazaApp Değişiklik Notları

**Son Güncelleme:** 9 Ocak 2026 05:40

---

## ✅ FAZ 3: PROFİL DÜZENLEME (Web + Mobil)

### Backend
| Dosya | Değişiklik |
|-------|------------|
| `AuthRestController.java` | PUT /api/auth/profil endpoint |

### Web (Mevcut)
| Dosya | Değişiklik |
|-------|------------|
| `HesabimController.java` | profilGuncelle() metodu |
| `hesabim.html` | Profil düzenleme formu |

### Mobil (YENİ)
| Dosya | Değişiklik |
|-------|------------|
| `profil-duzenle.tsx` | ✨ Yeni ekran (form) |
| `authStore.ts` | updateUser fonksiyonu eklendi |
| `hesabim.tsx` | "Profil Düzenle" menu item |

---

## 🎬 VIDEO BACKGROUND (Web + Mobil)
- Web: `index.html` - Video tag + dark overlay (%35)
- Mobil: `(tabs)/index.tsx` - expo-av Video component

---

## 📋 FAZ 3 DİĞER ÖZELLİKLER (Bekliyor)
1. Sipariş Takibi (kargo durumu)
2. Push Notification
3. Ödeme Sistemi

---

## 🧪 TEST KOMUTLARI

### Backend
```bash
cd c:\Users\MONSTER\Desktop\magaza-projesi
./mvnw spring-boot:run
```

### Mobil
```bash
cd c:\Users\MONSTER\Desktop\magaza-projesi\magaza-mobil
npx expo start --tunnel
```

---

## 📌 KURAL
> **HER ÖZELLİK HEM WEB HEM MOBİL İÇİN EKLENMELİ!**
