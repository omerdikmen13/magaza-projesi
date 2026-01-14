# 🎓 MOBİLDEN BACKEND'E: ADIM ADIM AKIŞ SENARYOLARI (DETAYLI VERSİYON)

Bu doküman, Mobil uygulamadan Spring Boot Backend'e bir isteğin nasıl gittiğini **kod satırlarıyla, açıklamalarla ve örneklerle** anlatan kapsamlı bir rehberdir.

---

## 📐 MİMARİ GENEL BAKIŞ

### Katmanlı Mimari Şeması

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           MOBİL UYGULAMA (React Native)                     │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐    ┌───────────┐ │
│  │   EKRANLAR   │───▶│   API.TS     │───▶│  APICLIENT   │───▶│   HTTP    │ │
│  │  login.tsx   │    │  authApi     │    │   axios      │    │  REQUEST  │ │
│  │  sepet.tsx   │    │  sepetApi    │    │  + Token     │    │           │ │
│  └──────────────┘    └──────────────┘    └──────────────┘    └─────┬─────┘ │
└───────────────────────────────────────────────────────────────────┬─────────┘
                                                                    │
                        ══════════════ İNTERNET ══════════════      │
                                                                    │
┌───────────────────────────────────────────────────────────────────▼─────────┐
│                         SPRING BOOT BACKEND                                 │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐    ┌───────────┐ │
│  │ CONTROLLER   │◀───│   SERVICE    │◀───│  REPOSITORY  │◀───│  MODEL    │ │
│  │ @RestController   │  (İş Mantığı) │    │  JPA/Hibernate    │  @Entity  │ │
│  │ @PostMapping │    │              │    │  .save()     │    │           │ │
│  └──────────────┘    └──────────────┘    └──────────────┘    └─────┬─────┘ │
│                                                                    │       │
│  ┌─────────────────────────────────────────────────────────────────▼─────┐ │
│  │                        VERİTABANI (MySQL)                             │ │
│  │   kullanici | urun | sepet | siparis_fisi | siparis_detay | ...       │ │
│  └───────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Teknolojiler ve Görevleri

| Teknoloji | Katman | Görevi |
|-----------|--------|--------|
| **React Native** | Mobil UI | Kullanıcı arayüzü, buton tıklamaları |
| **Axios** | HTTP Client | REST API istekleri oluşturma |
| **Spring Boot** | Backend Framework | HTTP isteklerini karşılama, iş mantığı |
| **JPA/Hibernate** | ORM | Java nesnelerini SQL'e çevirme |
| **MySQL** | Veritabanı | Verileri kalıcı olarak saklama |

---

## 🔐 SENARYO 1: KULLANICI GİRİŞİ (LOGIN) - EN DETAYLI

Kullanıcı, login ekranında kullanıcı adı ve şifresini girip "Giriş Yap" butonuna bastığında neler olur?

---

### 📱 ADIM 1: Mobil Ekran - Buton Tıklanır
**Dosya:** `magaza-mobil/app/(auth)/login.tsx`

Kullanıcı formu doldurur ve "Giriş Yap" butonuna basar.

```tsx
// login.tsx - Satır 30-32: State tanımları
const [kullaniciAdi, setKullaniciAdi] = useState('');  // "musteri1"
const [sifre, setSifre] = useState('');                 // "123456"

// login.tsx - Satır 188-189: Buton tıklanır
<TouchableOpacity onPress={handleLogin}>
    <Text>Giriş Yap</Text>
</TouchableOpacity>
```

**Açıklama:**
- `useState` → React'in state yönetimi. Kullanıcının girdiği değerleri tutar.
- `onPress={handleLogin}` → Butona basınca `handleLogin` fonksiyonu çalışır.

---

### 📱 ADIM 2: handleLogin Fonksiyonu
**Dosya:** `magaza-mobil/app/(auth)/login.tsx` - **Satır 72-78**

```tsx
const handleLogin = () => {
    // Boş kontrolü
    if (!kullaniciAdi.trim() || !sifre.trim()) {
        Alert.alert('Hata', 'Lütfen kullanıcı adı ve şifre girin');
        return;
    }
    
    loginMutation.mutate();  // ← API çağrısını başlat
};
```

**Açıklama:**
- `trim()` → Baştaki ve sondaki boşlukları siler.
- `loginMutation.mutate()` → React Query'nin mutation'ını tetikler (API çağrısı başlar).

---

### 📱 ADIM 3: Mutation Tanımı (API Çağrısı Hazırlığı)
**Dosya:** `magaza-mobil/app/(auth)/login.tsx` - **Satır 35-70**

```tsx
const loginMutation = useMutation({
    // API'yi çağıran fonksiyon
    mutationFn: () => authApi.login(kullaniciAdi, sifre),
    
    // BAŞARILI olursa bu çalışır
    onSuccess: async (data) => {
        const user = data.kullanici;        // Backend'den gelen kullanıcı bilgisi
        const token = data.token;           // Backend'den gelen token
        
        setUser(user, token);               // Zustand store'a kaydet
        
        // Role göre yönlendirme
        if (user.rol === 'ADMIN') {
            router.replace('/admin');
        } else if (user.rol === 'MAGAZA_SAHIBI') {
            router.replace('/sahip');
        } else {
            router.replace('/(tabs)');       // Ana sayfaya git
        }
    },
    
    // HATA olursa bu çalışır
    onError: (error) => {
        Alert.alert('Giriş Başarısız', error.response?.data?.error || 'Hata!');
    },
});
```

**Açıklama:**
- `useMutation` → React Query'nin POST/PUT/DELETE istekleri için hook'u.
- `mutationFn` → Asıl API çağrısını yapan fonksiyon.
- `onSuccess` → HTTP 200 dönerse çalışır.
- `onError` → HTTP 400/401/500 dönerse çalışır.

---

### 📡 ADIM 4: API Servisi
**Dosya:** `magaza-mobil/services/api.ts` - **Satır 4-8**

```typescript
export const authApi = {
    login: async (kullaniciAdi: string, sifre: string) => {
        const response = await apiClient.post('/api/auth/login', { 
            kullaniciAdi, 
            sifre 
        });
        return response.data;
    },
};
```

**Açıklama:**
- `apiClient.post(URL, BODY)` → Axios ile POST isteği yapar.
- İlk parametre: URL (`/api/auth/login`)
- İkinci parametre: Request Body (JSON olarak gönderilecek veri)

---

### 🌐 ADIM 5: API Client (HTTP İsteği Oluşturma)
**Dosya:** `magaza-mobil/services/apiClient.ts` - **Satır 7-17**

```typescript
// Base URL tanımı
const BASE_URL = 'http://13.60.76.224:8080';

// Axios instance oluştur
export const apiClient = axios.create({
    baseURL: BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
    timeout: 15000,  // 15 saniye timeout
});
```

**Ne oluşturuluyor?**
```
HTTP İSTEĞİ:
-----------
POST http://13.60.76.224:8080/api/auth/login
Content-Type: application/json

{
    "kullaniciAdi": "musteri1",
    "sifre": "123456"
}
```

---

### 🖥️ ADIM 6: Backend - İstek Karşılanır
**Dosya:** `AuthRestController.java` - **Satır 15-18 ve 79-80**

```java
@RestController                    // Bu sınıf bir REST API controller'ıdır
@RequestMapping("/api/auth")       // Tüm URL'ler /api/auth ile başlar
@CrossOrigin(origins = "*")        // Tüm origin'lerden gelen istekleri kabul et
public class AuthRestController {

    @PostMapping("/login")         // URL: POST /api/auth/login
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // request nesnesi otomatik olarak JSON'dan oluşturulur
    }
}
```

**Spring Anotasyonları:**

| Anotasyon | Açıklama |
|-----------|----------|
| `@RestController` | Bu sınıf JSON döndüren bir API controller'ıdır (HTML değil) |
| `@RequestMapping("/api/auth")` | Tüm metodlar için URL prefix'i |
| `@PostMapping("/login")` | Sadece HTTP POST metoduyla çalışır |
| `@RequestBody` | JSON body'yi otomatik olarak Java nesnesine çevirir |
| `@CrossOrigin` | CORS (Cross-Origin Resource Sharing) izni |

---

### 🖥️ ADIM 7: Kullanıcıyı Veritabanından Bul
**Dosya:** `AuthRestController.java` - **Satır 82-86**

```java
Optional<Kullanici> kullaniciOpt = kullaniciRepository.findByKullaniciAdi(request.getKullaniciAdi());

if (kullaniciOpt.isEmpty()) {
    return ResponseEntity.badRequest().body(Map.of("error", "Kullanıcı bulunamadı"));
}

Kullanici kullanici = kullaniciOpt.get();
```

**Hibernate'in Oluşturduğu SQL:**
```sql
SELECT * FROM kullanici WHERE kullanici_adi = 'musteri1'
```

**Açıklama:**
- `findByKullaniciAdi(...)` → JPA'nın "magic method"u. Metod adından SQL oluşturur.
- `Optional<>` → Null güvenliği sağlar. Kullanıcı yoksa `empty`, varsa `present`.

---

### 🖥️ ADIM 8: Şifre Kontrolü (Hash Karşılaştırma)
**Dosya:** `AuthRestController.java` - **Satır 90-93**

```java
// Düz metin şifreyi, veritabanındaki hashlenmiş şifreyle karşılaştır
if (!passwordEncoder.matches(request.getSifre(), kullanici.getSifre())) {
    return ResponseEntity.badRequest().body(Map.of("error", "Şifre hatalı"));
}
```

**Açıklama:**
- `passwordEncoder.matches("123456", "$2a$10$xyz...")` → BCrypt hash kontrolü
- Veritabanında şifreler **HASHLENMIŞ** tutuluyor (örn: `$2a$10$...`)
- `matches()` fonksiyonu düz metni hash'leyip karşılaştırır

**Neden Hash?**
> Şifreler düz metin olarak saklanmaz çünkü veritabanı sızarsa tüm şifreler ele geçer. Hash'leme tek yönlüdür, geri döndürülemez.

---

### 🖥️ ADIM 9: Token Üretimi ve Cevap Dönme
**Dosya:** `AuthRestController.java` - **Satır 100-105**

```java
Map<String, Object> response = new HashMap<>();
response.put("message", "Giriş başarılı");
response.put("kullanici", createUserResponse(kullanici));  // Kullanıcı bilgileri
response.put("token", "simple-token-" + kullanici.getId()); // Token: "simple-token-5"

return ResponseEntity.ok(response);  // HTTP 200 OK
```

**HTTP Cevabı:**
```json
HTTP 200 OK
Content-Type: application/json

{
    "message": "Giriş başarılı",
    "kullanici": {
        "id": 5,
        "kullaniciAdi": "musteri1",
        "email": "musteri@test.com",
        "ad": "Ahmet",
        "soyad": "Yılmaz",
        "rol": "MUSTERI"
    },
    "token": "simple-token-5"
}
```

---

### 📱 ADIM 10: Mobil - Token ve Kullanıcıyı Kaydet
**Dosya:** `magaza-mobil/stores/authStore.ts` - **Satır 47-55**

```typescript
setUser: (user: User, token: string) => {
    set({
        user,                    // Kullanıcı bilgisi
        token,                   // "simple-token-5"
        isAuthenticated: true,   // Giriş yapılmış
    });
},
```

**Açıklama:**
- `Zustand` → React için basit state yönetim kütüphanesi
- `persist` middleware → Token'ı `AsyncStorage`'a kaydeder (uygulama kapansa bile kalır)
- Sonraki tüm API isteklerinde bu token kullanılır

---

### 📱 ADIM 11: Ana Sayfaya Yönlendirme
**Dosya:** `login.tsx` - **Satır 54-61**

```tsx
// Role göre yönlendirme
if (user.rol === 'ADMIN') {
    router.replace('/admin');
} else if (user.rol === 'MAGAZA_SAHIBI') {
    router.replace('/sahip');
} else {
    router.replace('/(tabs)');  // Müşteri ana sayfası
}
```

---

## 🛒 SENARYO 2: SEPETE ÜRÜN EKLEME - DETAYLI

---

### ADIM 1: Kullanıcı "Sepete Ekle" Butonuna Basar
**Dosya:** `urun/[id].tsx` (Ürün Detay Sayfası)

```tsx
const handleSepeteEkle = async () => {
    await sepetApi.add(urunId, selectedBedenId, 1);
    Alert.alert('Başarılı', 'Ürün sepete eklendi!');
};
```

---

### ADIM 2: API Servisi
**Dosya:** `api.ts` - **Satır 95-98**

```typescript
add: async (urunId: number, bedenId: number, adet: number = 1) => {
    const response = await apiClient.post('/api/sepet/ekle', { 
        urunId,   // 12
        bedenId,  // 2 (M bedeni)
        adet      // 1
    });
    return response.data;
},
```

---

### ADIM 3: API Client - Token Ekleme (Interceptor)
**Dosya:** `apiClient.ts` - **Satır 21-32**

```typescript
apiClient.interceptors.request.use(
    (config) => {
        // Zustand store'dan token'ı al
        const token = useAuthStore.getState().token;  // "simple-token-5"
        
        if (token) {
            // Header'a ekle
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    }
);
```

**Oluşan HTTP İsteği:**
```
POST http://13.60.76.224:8080/api/sepet/ekle
Authorization: Bearer simple-token-5    ← TOKEN BURADA
Content-Type: application/json

{
    "urunId": 12,
    "bedenId": 2,
    "adet": 1
}
```

---

### ADIM 4: Backend Controller
**Dosya:** `SepetRestController.java` - **Satır 71-73**

```java
@PostMapping("/ekle")
public ResponseEntity<?> sepeteEkle(
    @RequestHeader("Authorization") String token,   // "Bearer simple-token-5"
    @RequestBody SepetEkleRequest request) {        // { urunId: 12, bedenId: 2, adet: 1 }
```

---

### ADIM 5: Token'dan Kullanıcıyı Bul
**Dosya:** `SepetRestController.java` - **Satır 298-311**

```java
private Kullanici getKullaniciFromToken(String token) {
    // "Bearer simple-token-5" → "simple-token-5"
    String tokenValue = token.substring(7);
    
    // "simple-token-5" → 5
    Long userId = Long.parseLong(tokenValue.replace("simple-token-", ""));
    
    // Veritabanından kullanıcıyı bul
    return kullaniciRepository.findById(userId).orElse(null);
}
```

**SQL:**
```sql
SELECT * FROM kullanici WHERE id = 5
```

---

### ADIM 6: Ürün ve Beden Kontrolü
**Dosya:** `SepetRestController.java` - **Satır 80-88**

```java
// Ürünü bul (yoksa hata fırlat)
Urun urun = urunRepository.findById(request.getUrunId())
        .orElseThrow(() -> new RuntimeException("Ürün bulunamadı"));

// Bedeni bul
Beden beden = bedenRepository.findById(request.getBedenId())
        .orElseThrow(() -> new RuntimeException("Beden bulunamadı"));

// Stok kontrolü
UrunStok stok = urunStokRepository.findByUrunIdAndBedenId(
    request.getUrunId(), request.getBedenId()
).orElseThrow(() -> new RuntimeException("Stok bilgisi bulunamadı"));

if (stok.getAdet() < request.getAdet()) {
    return ResponseEntity.badRequest()
        .body(Map.of("error", "Yetersiz stok! Mevcut: " + stok.getAdet()));
}
```

---

### ADIM 7: Sepete Ekle veya Güncelle
**Dosya:** `SepetRestController.java` - **Satır 95-115**

```java
// Bu ürün zaten sepette var mı?
Optional<Sepet> mevcutSepet = sepetRepository.findByKullaniciIdAndUrunIdAndBedenId(
    kullanici.getId(), request.getUrunId(), request.getBedenId()
);

Sepet sepet;
if (mevcutSepet.isPresent()) {
    // VARSA: Adet artır
    sepet = mevcutSepet.get();
    sepet.setAdet(sepet.getAdet() + request.getAdet());  // 1 + 1 = 2
} else {
    // YOKSA: Yeni sepet satırı oluştur
    sepet = new Sepet();
    sepet.setKullanici(kullanici);
    sepet.setUrun(urun);
    sepet.setBeden(beden);
    sepet.setAdet(request.getAdet());
}

sepetRepository.save(sepet);  // Veritabanına kaydet
```

**SQL (Yeni ekleme):**
```sql
INSERT INTO sepet (kullanici_id, urun_id, beden_id, adet) VALUES (5, 12, 2, 1)
```

**SQL (Güncelleme):**
```sql
UPDATE sepet SET adet = 2 WHERE id = 45
```

---

### ADIM 8: Response Dönme
**Dosya:** `SepetRestController.java` - **Satır 117-119**

```java
return ResponseEntity.ok(Map.of(
    "message", "Ürün sepete eklendi",
    "sepetItem", createSepetResponse(sepet)
));
```

---

## 💳 SENARYO 3: SİPARİŞ VERME (KRİTİK: @Transactional)

---

### AKIŞ ÖZETİ:

```
SEPET → SİPARİŞ FİŞİ → SİPARİŞ DETAYLARI → STOK DÜŞME → SEPET TEMİZLEME
```

### Controller Metodu
**Dosya:** `SepetRestController.java` - **Satır 211-295**

```java
@PostMapping("/siparis-ver")
@Transactional  // ← ÇOK ÖNEMLİ!
public ResponseEntity<?> siparisVer(...) {
    
    // 1. Kullanıcının sepetini al
    List<Sepet> sepetListesi = sepetRepository.findByKullaniciId(kullanici.getId());
    
    // 2. Tek mağaza kontrolü (farklı mağazalardan sipariş verilemez)
    Magaza magaza = sepetListesi.get(0).getUrun().getMagaza();
    for (Sepet item : sepetListesi) {
        if (!item.getUrun().getMagaza().getId().equals(magaza.getId())) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Farklı mağazalardan sipariş verilemez"));
        }
    }
    
    // 3. Stok kontrolü
    for (Sepet item : sepetListesi) {
        UrunStok stok = urunStokRepository.findByUrunIdAndBedenId(...).get();
        if (stok.getAdet() < item.getAdet()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Yetersiz stok: " + item.getUrun().getAd()));
        }
    }
    
    // 4. Sipariş fişi oluştur
    SiparisFisi fis = new SiparisFisi();
    fis.setKullanici(kullanici);
    fis.setMagaza(magaza);
    fis.setDurum(SiparisDurum.BEKLEMEDE);
    siparisFisiRepository.save(fis);
    
    // 5. Detayları kaydet + Stok düş
    for (Sepet item : sepetListesi) {
        // Detay kaydet
        SiparisDetay detay = new SiparisDetay();
        detay.setSiparisFisi(fis);
        detay.setUrun(item.getUrun());
        detay.setAdet(item.getAdet());
        siparisDetayRepository.save(detay);
        
        // Stok düş
        UrunStok stok = urunStokRepository.findByUrunIdAndBedenId(...).get();
        stok.setAdet(stok.getAdet() - item.getAdet());  // 20 - 2 = 18
        urunStokRepository.save(stok);
    }
    
    // 6. Sepeti temizle
    sepetRepository.deleteByKullaniciId(kullanici.getId());
    
    return ResponseEntity.ok(Map.of("message", "Sipariş oluşturuldu"));
}
```

### @Transactional Ne Demek?

**SENARYO:** Kullanıcı 2 ürün sipariş ediyor.
1. Sipariş fişi oluşturuldu ✓
2. Ürün 1 için detay kaydedildi ✓
3. Ürün 1 için stok düşüldü ✓
4. Ürün 2 için detay kaydedilirken **HATA ÇIKTI!** ✗

**@Transactional OLMADAN:**
- Sipariş fişi veritabanında KALIR
- Ürün 1'in stoğu DÜŞER
- Ürün 2 eksik kalır
- **VERİ TUTARSIZLIĞI!**

**@Transactional İLE:**
- Hata çıkınca TÜM İŞLEMLER GERİ ALINIR (Rollback)
- Sipariş fişi SİLİNİR
- Stok ESKİ HALİNE DÖNER
- **VERİ TUTARLI KALIR!**

---

## 📊 HTTP METODLARİ VE ANOTASYONLAR

| HTTP Metod | Spring Anotasyonu | Kullanım |
|------------|-------------------|----------|
| GET | `@GetMapping` | Veri çekme (listeleme, detay) |
| POST | `@PostMapping` | Yeni veri oluşturma (kayıt, ekleme) |
| PUT | `@PutMapping` | Var olan veriyi güncelleme |
| DELETE | `@DeleteMapping` | Veri silme |

---

## 🔑 ÖNEMLİ KAVRAMLAR (SINAV İÇİN)

### 1. @RestController vs @Controller
- `@Controller` → HTML sayfa döndürür (Thymeleaf ile)
- `@RestController` → JSON döndürür (API için)

### 2. @RequestBody vs @RequestParam
- `@RequestBody` → JSON body'den veri alır: `{ "urunId": 5 }`
- `@RequestParam` → URL'den parametre alır: `/search?q=tişört`

### 3. ResponseEntity Nedir?
HTTP cevabı döndürür. Status code + Body içerir.
```java
ResponseEntity.ok(data);              // 200 OK
ResponseEntity.badRequest().body(x);  // 400 Bad Request
ResponseEntity.status(401).body(x);   // 401 Unauthorized
```

### 4. JPA Repository "Magic Methods"
Metod isimlerinden otomatik SQL üretir:
```java
findByKullaniciAdi(...)         // WHERE kullanici_adi = ?
findByUrunIdAndBedenId(...)     // WHERE urun_id = ? AND beden_id = ?
deleteByKullaniciId(...)        // DELETE FROM ... WHERE kullanici_id = ?
```

### 5. Optional<> Nedir?
Null güvenliği sağlar:
```java
Optional<Kullanici> opt = repo.findById(5);
if (opt.isPresent()) { ... }     // Varsa
if (opt.isEmpty()) { ... }       // Yoksa
opt.orElseThrow(() -> ...)       // Yoksa hata fırlat
```

---

## ❓ HOCA SORULARI

**S1: Mobilden istek nasıl gidiyor?**
> Axios kütüphanesi HTTP isteği oluşturuyor. apiClient.ts'deki interceptor Token ekliyor. JSON body ile POST isteği gönderiliyor.

**S2: Backend bu isteği nasıl karşılıyor?**
> Spring'in DispatcherServlet'i URL ve HTTP metoduna bakarak uygun @RestController metodunu buluyor.

**S3: Token ne işe yarıyor?**
> Kullanıcının kim olduğunu sunucuya söylüyor. Her istekte Header'da gönderiliyor. Sunucu token'dan kullanıcı ID'sini çıkarıp veritabanından kullanıcıyı buluyor.

**S4: Şifreler nasıl saklanıyor?**
> BCrypt algoritmasıyla hashlenerek saklanıyor. Düz metin asla veritabanına yazılmıyor.

**S5: @Transactional ne demek?**
> Birden fazla veritabanı işlemini tek bir işlem gibi yönetiyor. Hata olursa tümü geri alınıyor (rollback).

**S6: Repository'de SQL yazmadın, nasıl çalışıyor?**
> JPA/Hibernate, metod isimlerinden otomatik SQL üretiyor. `findByKullaniciAdi()` → `SELECT * FROM ... WHERE kullanici_adi = ?`
