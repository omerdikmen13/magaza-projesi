# SINAV NOTLARI - Git ve Mobil-Backend Bağlantısı

---

## BÖLÜM 1: GIT KOMUTLARI

### İlk Kez GitHub'a Yükleme (Bir Seferlik)

```bash
# 1. Git başlat
git init

# 2. Tüm dosyaları ekle
git add .

# 3. Commit yap (kaydet)
git commit -m "ilk commit"

# 4. Branch adını main yap
git branch -M main

# 5. GitHub repo bağla
git remote add origin https://github.com/KULLANICI/REPO.git

# 6. GitHub'a gönder
git push -u origin main
```

### Değişiklik Yaptıktan Sonra Güncelleme (Her Seferinde)

```bash
# 1. Değişen dosyaları ekle
git add .

# 2. Commit yap
git commit -m "değişiklik mesajı"

# 3. GitHub'a gönder
git push origin main
```

**ÖNEMLİ:** Git sadece DEĞİŞEN dosyaları gönderir, tüm projeyi tekrar yüklemez!

---

## BÖLÜM 2: MOBİL ↔ BACKEND BAĞLANTISI

### Genel Akış

```
┌─────────────────┐     HTTP İsteği      ┌─────────────────┐
│  React Native   │ ──────────────────►  │  Spring Boot    │
│  (Mobil App)    │                      │  (Backend)      │
│                 │ ◄──────────────────  │                 │
└─────────────────┘     JSON Yanıt       └─────────────────┘
```

### Örnek: Ürün Listeleme

**1. MOBİL TARAF (React Native - TypeScript)**

Dosya: `magaza-mobil/services/api.ts`

```typescript
// API çağrısı yapan fonksiyon
export const urunlerApi = {
    getAll: async () => {
        const response = await apiClient.get('/api/urunler');
        return response.data;
    }
};
```

Dosya: `magaza-mobil/app/(tabs)/index.tsx`

```typescript
// Ürünleri çeken React bileşeni
const { data: urunler } = useQuery({
    queryKey: ['urunler'],
    queryFn: urunlerApi.getAll
});
```

---

**2. BACKEND TARAF (Spring Boot - Java)**

### Controller (API Endpoint)

Dosya: `src/main/java/com/magazaapp/controller/api/UrunRestController.java`

```java
@RestController
@RequestMapping("/api/urunler")
public class UrunRestController {

    @Autowired
    private UrunRepository urunRepository;

    // GET /api/urunler - Tüm ürünleri getir
    @GetMapping
    public ResponseEntity<List<Urun>> tumUrunler() {
        List<Urun> urunler = urunRepository.findAll();
        return ResponseEntity.ok(urunler);
    }
}
```

### Model (Entity)

Dosya: `src/main/java/com/magazaapp/model/Urun.java`

```java
@Entity
@Table(name = "urunler")
public class Urun {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String ad;
    private BigDecimal fiyat;
    private String aciklama;
    private String resimUrl;
    
    @ManyToOne
    private Magaza magaza;
    
    @ManyToOne
    private AltKategori altKategori;
    
    // Getter ve Setter'lar...
}
```

### Repository (Veritabanı İşlemleri)

Dosya: `src/main/java/com/magazaapp/repository/UrunRepository.java`

```java
@Repository
public interface UrunRepository extends JpaRepository<Urun, Long> {
    
    // Mağazaya göre ürün bul
    List<Urun> findByMagaza(Magaza magaza);
    
    // Kategoriye göre ürün bul
    List<Urun> findByAltKategori(AltKategori altKategori);
    
    // Ürün ara
    List<Urun> findByAdContainingIgnoreCase(String arama);
}
```

---

## BÖLÜM 3: KATMANLAR ARASI İLİŞKİ

```
┌──────────────────────────────────────────────────────────┐
│                     MOBİL (React Native)                 │
│  useQuery → api.ts → axios.get('/api/urunler')          │
└──────────────────────────────────────────────────────────┘
                           │
                           ▼ HTTP GET
┌──────────────────────────────────────────────────────────┐
│                CONTROLLER (Spring Boot)                  │
│  @GetMapping → urunRepository.findAll()                 │
└──────────────────────────────────────────────────────────┘
                           │
                           ▼
┌──────────────────────────────────────────────────────────┐
│                REPOSITORY (JPA)                          │
│  JpaRepository → SELECT * FROM urunler                  │
└──────────────────────────────────────────────────────────┘
                           │
                           ▼
┌──────────────────────────────────────────────────────────┐
│                   MODEL (Entity)                         │
│  @Entity Urun → MySQL Tablosu                           │
└──────────────────────────────────────────────────────────┘
                           │
                           ▼
┌──────────────────────────────────────────────────────────┐
│                   DATABASE (MySQL)                       │
│  urunler tablosu                                        │
└──────────────────────────────────────────────────────────┘
```

---

## BÖLÜM 4: SEPETE EKLEME ÖRNEĞİ (POST İsteği)

### Mobil Taraf

```typescript
// api.ts
export const sepetApi = {
    add: async (urunId: number, bedenId: number, adet: number) => {
        const response = await apiClient.post('/api/sepet/ekle', {
            urunId,
            bedenId,
            adet
        });
        return response.data;
    }
};
```

### Backend Controller

```java
@PostMapping("/ekle")
public ResponseEntity<?> sepeteEkle(
        @RequestHeader("Authorization") String token,
        @RequestBody SepetEkleRequest request) {
    
    // Token'dan kullanıcıyı bul
    Kullanici kullanici = getKullaniciFromToken(token);
    
    // Ürün ve beden bul
    Urun urun = urunRepository.findById(request.getUrunId()).orElse(null);
    Beden beden = bedenRepository.findById(request.getBedenId()).orElse(null);
    
    // Sepete ekle
    Sepet sepet = new Sepet(kullanici, urun, beden, request.getAdet());
    sepetRepository.save(sepet);
    
    return ResponseEntity.ok(Map.of("message", "Ürün sepete eklendi"));
}
```

---

## BÖLÜM 5: ÖNEMLİ ANNOTASYONLAR

| Annotation | Açıklama |
|------------|----------|
| `@RestController` | REST API controller |
| `@RequestMapping` | URL path tanımla |
| `@GetMapping` | HTTP GET isteği |
| `@PostMapping` | HTTP POST isteği |
| `@RequestBody` | JSON body'yi objeye çevir |
| `@RequestHeader` | Header'dan değer al (token gibi) |
| `@Entity` | Veritabanı tablosu |
| `@Repository` | Veritabanı işlemleri |
| `@Autowired` | Dependency injection |

---

## ÖZET

1. **Mobil** → `apiClient.get/post()` ile HTTP isteği gönderir
2. **Controller** → İsteği karşılar, işler
3. **Repository** → Veritabanı işlemi yapar
4. **Model** → Veri yapısını tanımlar
5. **Database** → Verileri saklar

**Yanıt JSON formatında mobil'e döner!**
