# SINAV NOTLARI - Spring Boot Mimarisi ve Mobil-Backend Bağlantısı

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
git remote add origin https://github.com/omerdikmen13/magaza-projesi.git

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

## BÖLÜM 2: MOBİL ↔ BACKEND BAĞLANTISI (DETAYLI)

### 2.1 Genel Mimari Akışı

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              KULLANICI                                      │
│                         (Telefondan tıklar)                                 │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                        REACT NATIVE (MOBİL APP)                             │
│  ┌─────────────┐    ┌──────────────┐    ┌─────────────────┐                │
│  │   Screen    │ →  │   useQuery   │ →  │   api.ts        │                │
│  │ (Ekran)     │    │ (Hook)       │    │ (HTTP İsteği)   │                │
│  └─────────────┘    └──────────────┘    └─────────────────┘                │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    │ HTTP Request (GET/POST/PUT/DELETE)
                                    │ Content-Type: application/json
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                          SPRING BOOT BACKEND                                │
│                                                                             │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐         │
│  │   Controller    │ →  │    Service      │ →  │   Repository    │         │
│  │  (API Endpoint) │    │ (İş Mantığı)    │    │ (DB Sorguları)  │         │
│  └─────────────────┘    └─────────────────┘    └─────────────────┘         │
│                                                          │                  │
│                                                          ▼                  │
│                                              ┌─────────────────┐            │
│                                              │     Model       │            │
│                                              │    (Entity)     │            │
│                                              └─────────────────┘            │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    │ JPA/Hibernate SQL
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              MySQL DATABASE                                  │
│   ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐     │
│   │ urunler  │  │ magaza   │  │kullanici │  │  sepet   │  │siparisler│     │
│   └──────────┘  └──────────┘  └──────────┘  └──────────┘  └──────────┘     │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

### 2.2 Profesyonel Katmanlı Mimari (3-Layer Architecture)

```
┌────────────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER (Sunum)                       │
│                                                                      │
│  @RestController / @Controller                                       │
│  - HTTP isteklerini karşılar                                        │
│  - İstek parametrelerini validate eder                              │
│  - Sonucu JSON olarak döner                                         │
│  - İş mantığı İÇERMEZ! (Service'e delege eder)                      │
└────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌────────────────────────────────────────────────────────────────────┐
│                      SERVICE LAYER (İş Mantığı)                     │
│                                                                      │
│  @Service                                                            │
│  - Tüm iş mantığı burada                                            │
│  - Validasyon kuralları                                             │
│  - Transaction yönetimi (@Transactional)                            │
│  - Birden fazla Repository'yi koordine eder                         │
└────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌────────────────────────────────────────────────────────────────────┐
│                    DATA ACCESS LAYER (Veri Erişim)                  │
│                                                                      │
│  @Repository                                                         │
│  - Veritabanı işlemleri (CRUD)                                      │
│  - JPA Query Methods                                                │
│  - Custom JPQL Queries                                              │
└────────────────────────────────────────────────────────────────────┘
```

**NEDEN 3 KATMAN?**
1. **Separation of Concerns** - Her katman tek bir iş yapar
2. **Reusability** - Service methodları web/mobil/API'da tekrar kullanılır
3. **Testability** - Her katman ayrı test edilebilir
4. **Maintainability** - Kod değişikliği kolay ve güvenli

---

### 2.3 Mobil Taraf - React Native (TypeScript)

#### 2.3.1 API Client Yapılandırması

**Dosya:** `magaza-mobil/services/api.ts`

```typescript
import axios from 'axios';
import AsyncStorage from '@react-native-async-storage/async-storage';

// API base URL - Backend adresi
const API_BASE_URL = 'http://192.168.1.100:8080'; // Bilgisayar IP'si

// Axios instance oluştur
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Her istekte token ekle (interceptor)
apiClient.interceptors.request.use(async (config) => {
  const token = await AsyncStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default apiClient;
```

#### 2.3.2 API Servisleri

**Ürün API'si:**
```typescript
// services/urunApi.ts
export const urunlerApi = {
  
  // Tüm ürünleri getir
  getAll: async (): Promise<Urun[]> => {
    const response = await apiClient.get('/api/urunler');
    return response.data;
  },
  
  // Tek ürün getir
  getById: async (id: number): Promise<Urun> => {
    const response = await apiClient.get(`/api/urunler/${id}`);
    return response.data;
  },
  
  // Mağazaya göre ürünler
  getByMagaza: async (magazaId: number): Promise<Urun[]> => {
    const response = await apiClient.get(`/api/urunler/magaza/${magazaId}`);
    return response.data;
  },
  
  // Ürün ara
  search: async (query: string): Promise<Urun[]> => {
    const response = await apiClient.get(`/api/urunler/ara?q=${query}`);
    return response.data;
  }
};
```

**Sepet API'si:**
```typescript
// services/sepetApi.ts
export const sepetApi = {
  
  // Sepeti getir
  getCart: async (): Promise<SepetItem[]> => {
    const response = await apiClient.get('/api/sepet');
    return response.data;
  },
  
  // Sepete ekle
  add: async (urunId: number, bedenId: number, adet: number): Promise<void> => {
    await apiClient.post('/api/sepet/ekle', { urunId, bedenId, adet });
  },
  
  // Sepetten çıkar
  remove: async (sepetItemId: number): Promise<void> => {
    await apiClient.delete(`/api/sepet/${sepetItemId}`);
  },
  
  // Miktarı güncelle
  updateQuantity: async (sepetItemId: number, adet: number): Promise<void> => {
    await apiClient.put(`/api/sepet/${sepetItemId}`, { adet });
  }
};
```

#### 2.3.3 React Query ile Veri Çekme

**Dosya:** `app/(tabs)/index.tsx`

```typescript
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { urunlerApi } from '../services/urunApi';
import { sepetApi } from '../services/sepetApi';

export default function HomeScreen() {
  const queryClient = useQueryClient();

  // Ürünleri çek (GET)
  const { data: urunler, isLoading, error } = useQuery({
    queryKey: ['urunler'],
    queryFn: urunlerApi.getAll,
  });

  // Sepete ekle (POST) - Mutation
  const sepeteEkleMutation = useMutation({
    mutationFn: ({ urunId, bedenId, adet }: SepetEkleParams) => 
      sepetApi.add(urunId, bedenId, adet),
    onSuccess: () => {
      // Sepeti yeniden çek
      queryClient.invalidateQueries({ queryKey: ['sepet'] });
      Alert.alert('Başarılı', 'Ürün sepete eklendi!');
    },
    onError: (error: any) => {
      Alert.alert('Hata', error.response?.data?.message || 'Bir hata oluştu');
    },
  });

  // Kullanım
  const handleSepeteEkle = (urunId: number) => {
    sepeteEkleMutation.mutate({ urunId, bedenId: 1, adet: 1 });
  };

  if (isLoading) return <ActivityIndicator />;
  if (error) return <Text>Hata: {error.message}</Text>;

  return (
    <FlatList
      data={urunler}
      renderItem={({ item }) => (
        <ProductCard 
          urun={item} 
          onSepeteEkle={() => handleSepeteEkle(item.id)} 
        />
      )}
    />
  );
}
```

---

### 2.4 Backend Taraf - Spring Boot (Java)

#### 2.4.1 REST Controller (API Endpoint)

**Dosya:** `src/main/java/com/magazaapp/controller/api/UrunRestController.java`

```java
@RestController
@RequestMapping("/api/urunler")
public class UrunRestController {

    private final UrunService urunService;  // Service injection

    // Constructor Injection (önerilen yöntem)
    public UrunRestController(UrunService urunService) {
        this.urunService = urunService;
    }

    // GET /api/urunler - Tüm aktif ürünleri getir
    @GetMapping
    public ResponseEntity<List<Urun>> tumUrunler() {
        List<Urun> urunler = urunService.getAktifUrunler();
        return ResponseEntity.ok(urunler);
    }

    // GET /api/urunler/{id} - Tek ürün getir
    @GetMapping("/{id}")
    public ResponseEntity<Urun> urunGetir(@PathVariable Long id) {
        Urun urun = urunService.getUrunById(id);
        return ResponseEntity.ok(urun);
    }

    // GET /api/urunler/magaza/{magazaId} - Mağazanın ürünleri
    @GetMapping("/magaza/{magazaId}")
    public ResponseEntity<List<Urun>> magazaUrunleri(@PathVariable Long magazaId) {
        List<Urun> urunler = urunService.getUrunlerByMagaza(magazaId);
        return ResponseEntity.ok(urunler);
    }

    // GET /api/urunler/ara?q=... - Ürün ara
    @GetMapping("/ara")
    public ResponseEntity<List<Urun>> urunAra(@RequestParam String q) {
        List<Urun> sonuclar = urunService.araUrun(q);
        return ResponseEntity.ok(sonuclar);
    }
}
```

#### 2.4.2 Service Katmanı (İş Mantığı)

**Dosya:** `src/main/java/com/magazaapp/service/UrunService.java`

```java
@Service
public class UrunService {

    private final UrunRepository urunRepository;
    private final UrunStokRepository urunStokRepository;

    public UrunService(UrunRepository urunRepository, 
                       UrunStokRepository urunStokRepository) {
        this.urunRepository = urunRepository;
        this.urunStokRepository = urunStokRepository;
    }

    // Tüm aktif ürünleri getir
    public List<Urun> getAktifUrunler() {
        return urunRepository.findByAktifTrue();
    }

    // ID ile ürün getir
    public Urun getUrunById(Long id) {
        return urunRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Ürün bulunamadı"));
    }

    // Mağazaya göre ürünler
    public List<Urun> getUrunlerByMagaza(Long magazaId) {
        return urunRepository.findByMagazaIdAndAktifTrue(magazaId);
    }

    // Ürün ara
    public List<Urun> araUrun(String aramaKelimesi) {
        if (aramaKelimesi == null || aramaKelimesi.length() < 2) {
            return List.of();
        }
        
        return urunRepository.findByAktifTrue().stream()
            .filter(urun -> 
                urun.getAd().toLowerCase().contains(aramaKelimesi.toLowerCase()) ||
                urun.getAciklama().toLowerCase().contains(aramaKelimesi.toLowerCase())
            )
            .toList();
    }

    // Ürün kaydet - Transaction ile
    @Transactional
    public Urun saveUrun(Urun urun) {
        return urunRepository.save(urun);
    }

    // Stok güncelle - Transaction ile
    @Transactional
    public void updateStok(Long urunId, Long bedenId, int yeniAdet) {
        UrunStok stok = urunStokRepository
            .findByUrunIdAndBedenId(urunId, bedenId)
            .orElseThrow(() -> new RuntimeException("Stok bulunamadı"));
        
        stok.setAdet(yeniAdet);
        urunStokRepository.save(stok);
    }
}
```

#### 2.4.3 Repository Katmanı (Veritabanı)

**Dosya:** `src/main/java/com/magazaapp/repository/UrunRepository.java`

```java
@Repository
public interface UrunRepository extends JpaRepository<Urun, Long> {
    
    // Aktif ürünleri getir
    List<Urun> findByAktifTrue();
    
    // Mağazaya göre aktif ürünler
    List<Urun> findByMagazaIdAndAktifTrue(Long magazaId);
    
    // Mağazaya göre tüm ürünler
    List<Urun> findByMagazaId(Long magazaId);
    
    // Kategoriye göre ürünler
    List<Urun> findByAltKategoriKategoriId(Long kategoriId);
    
    // Ürün ara (ad içinde)
    List<Urun> findByAdContainingIgnoreCase(String arama);
    
    // Custom JPQL Query örneği
    @Query("SELECT u FROM Urun u WHERE u.fiyat BETWEEN :min AND :max AND u.aktif = true")
    List<Urun> findByFiyatAraligi(@Param("min") BigDecimal min, @Param("max") BigDecimal max);
}
```

#### 2.4.4 Model (Entity)

**Dosya:** `src/main/java/com/magazaapp/model/Urun.java`

```java
@Entity
@Table(name = "urunler")
public class Urun {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String ad;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal fiyat;
    
    @Column(length = 1000)
    private String aciklama;
    
    private String resimUrl;
    private String renk;
    private Boolean aktif = true;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "magaza_id")
    private Magaza magaza;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alt_kategori_id")
    private AltKategori altKategori;
    
    @Enumerated(EnumType.STRING)
    private Sezon sezon;
    
    @OneToMany(mappedBy = "urun", cascade = CascadeType.ALL)
    private List<UrunStok> stoklar;
    
    // Getter ve Setter'lar...
}
```

---

### 2.5 HTTP İstek-Yanıt Örnekleri

#### GET İsteği (Veri Çekme)

```
MOBİL:  GET /api/urunler HTTP/1.1
        Host: 192.168.1.100:8080
        Content-Type: application/json

BACKEND ALIR:
        UrunRestController.tumUrunler() çağrılır
        → UrunService.getAktifUrunler() çağrılır
        → UrunRepository.findByAktifTrue() çağrılır
        → MySQL: SELECT * FROM urunler WHERE aktif = true

BACKEND DÖNER:
        HTTP/1.1 200 OK
        Content-Type: application/json
        
        [
          {
            "id": 1,
            "ad": "Mavi Basic T-Shirt",
            "fiyat": 199.99,
            "resimUrl": "/images/tshirt.jpg",
            "magaza": { "id": 1, "ad": "Mavi" }
          },
          ...
        ]
```

#### POST İsteği (Veri Gönderme)

```
MOBİL:  POST /api/sepet/ekle HTTP/1.1
        Host: 192.168.1.100:8080
        Content-Type: application/json
        Authorization: Bearer eyJhbGciOiJIUzI1...
        
        {
          "urunId": 1,
          "bedenId": 3,
          "adet": 2
        }

BACKEND ALIR:
        SepetRestController.sepeteEkle() çağrılır
        → Token'dan kullanıcı çıkarılır
        → SepetService.sepeteEkle() çağrılır
        → Stok kontrolü yapılır
        → Sepet kaydedilir

BACKEND DÖNER:
        HTTP/1.1 200 OK
        
        {
          "success": true,
          "message": "Ürün sepete eklendi!",
          "sepetToplam": 2
        }
```

---

### 2.6 Authentication (Kimlik Doğrulama)

```
                                AUTHENTICATION AKIŞI
                                
┌─────────────┐                                           ┌─────────────┐
│   MOBİL     │  1. POST /api/auth/login                  │   BACKEND   │
│             │  {"username":"ali","password":"123"}      │             │
│             │  ─────────────────────────────────────►   │             │
│             │                                           │   ↓         │
│             │                                           │ Kullanıcı   │
│             │                                           │ kontrolü    │
│             │                                           │   ↓         │
│             │  2. {"token":"eyJhbGc...","user":{...}}   │ JWT Token   │
│             │  ◄─────────────────────────────────────   │ oluştur     │
│             │                                           │             │
│   ↓         │                                           │             │
│ Token'ı     │                                           │             │
│ AsyncStorage│                                           │             │
│ kaydet      │                                           │             │
│   ↓         │                                           │             │
│             │  3. GET /api/sepet                        │             │
│             │  Authorization: Bearer eyJhbGc...         │             │
│             │  ─────────────────────────────────────►   │             │
│             │                                           │   ↓         │
│             │                                           │ Token       │
│             │                                           │ doğrula     │
│             │  4. [sepet verileri]                      │   ↓         │
│             │  ◄─────────────────────────────────────   │ Veri dön    │
└─────────────┘                                           └─────────────┘
```

---

## BÖLÜM 3: ÖNEMLİ ANNOTASYONLAR VE AÇIKLAMALARI

### Spring Boot Annotasyonları

| Annotation | Katman | Açıklama | Örnek Kullanım |
|------------|--------|----------|----------------|
| `@RestController` | Controller | REST API controller, JSON döner | `@RestController public class ApiController` |
| `@Controller` | Controller | Web controller, HTML template döner | `@Controller public class WebController` |
| `@RequestMapping("/api")` | Controller | URL prefix tanımlar | Class seviyesinde |
| `@GetMapping("/path")` | Controller | HTTP GET endpoint | `@GetMapping("/urunler")` |
| `@PostMapping("/path")` | Controller | HTTP POST endpoint | `@PostMapping("/ekle")` |
| `@PutMapping("/path")` | Controller | HTTP PUT endpoint | `@PutMapping("/{id}")` |
| `@DeleteMapping("/path")` | Controller | HTTP DELETE endpoint | `@DeleteMapping("/{id}")` |
| `@PathVariable` | Controller | URL'den parametre al | `@PathVariable Long id` → `/api/urun/5` |
| `@RequestParam` | Controller | Query string'den al | `@RequestParam String q` → `?q=arama` |
| `@RequestBody` | Controller | JSON body'yi objeye çevir | `@RequestBody SepetRequest req` |
| `@RequestHeader` | Controller | Header'dan değer al | `@RequestHeader("Authorization")` |
| `@Service` | Service | İş mantığı sınıfı | `@Service public class UrunService` |
| `@Transactional` | Service | Transaction yönetimi | Method veya class üstünde |
| `@Repository` | Repository | Veritabanı erişim sınıfı | `@Repository public interface UrunRepo` |
| `@Entity` | Model | Veritabanı tablosu | `@Entity public class Urun` |
| `@Table(name="x")` | Model | Tablo adı belirle | `@Table(name = "urunler")` |
| `@Id` | Model | Primary key | `@Id private Long id` |
| `@GeneratedValue` | Model | Auto-increment | `@GeneratedValue(strategy = IDENTITY)` |
| `@ManyToOne` | Model | N-1 ilişki | Urun → Magaza |
| `@OneToMany` | Model | 1-N ilişki | Magaza → Urunler |
| `@Column` | Model | Kolon özellikleri | `@Column(nullable = false)` |

### JPA Repository Method Naming

```java
// Spring Data JPA - Method adından otomatik SQL üretir
public interface UrunRepository extends JpaRepository<Urun, Long> {
    
    // findBy + FieldName
    List<Urun> findByAd(String ad);        // WHERE ad = ?
    
    // Containing (LIKE %...%)
    List<Urun> findByAdContaining(String ad);  // WHERE ad LIKE '%?%'
    
    // IgnoreCase 
    List<Urun> findByAdIgnoreCase(String ad);  // Case-insensitive
    
    // And / Or
    List<Urun> findByAdAndFiyat(String ad, BigDecimal fiyat);  // WHERE ad=? AND fiyat=?
    
    // Between
    List<Urun> findByFiyatBetween(BigDecimal min, BigDecimal max);  // BETWEEN
    
    // OrderBy
    List<Urun> findByMagazaIdOrderByFiyatAsc(Long magazaId);  // ORDER BY fiyat ASC
    
    // Nested property
    List<Urun> findByMagazaAd(String magazaAd);  // JOIN ve WHERE
    
    // Boolean check
    List<Urun> findByAktifTrue();  // WHERE aktif = true
}
```

---

## BÖLÜM 4: SEPETE EKLEME AKIŞI (TAM ÖRNEK)

### Adım Adım Akış

```
1. KULLANICI → "Sepete Ekle" butonuna basar
                ↓
2. MOBİL (React Native)
   │   const handleSepeteEkle = async () => {
   │     await sepetApi.add(urunId, bedenId, 1);
   │   };
                ↓
3. HTTP İSTEĞİ
   │   POST /api/sepet/ekle
   │   {"urunId": 5, "bedenId": 2, "adet": 1}
   │   Authorization: Bearer eyJhbG...
                ↓
4. CONTROLLER (SepetRestController.java)
   │   @PostMapping("/ekle")
   │   public ResponseEntity<?> sepeteEkle(@RequestBody SepetRequest req, 
   │                                        Authentication auth) {
   │       String username = auth.getName();
   │       return sepetService.sepeteEkle(username, req);
   │   }
                ↓
5. SERVICE (SepetService.java)
   │   @Transactional
   │   public SepetEklemeResult sepeteEkle(String username, SepetRequest req) {
   │       // 1. Kullanıcıyı bul
   │       Kullanici k = kullaniciService.getByUsername(username);
   │       
   │       // 2. Ürün ve beden bul
   │       Urun urun = urunService.getUrunById(req.getUrunId());
   │       Beden beden = bedenService.getBedenById(req.getBedenId());
   │       
   │       // 3. Stok kontrolü
   │       UrunStok stok = urunStokRepo.findByUrunIdAndBedenId(...);
   │       if (stok.getAdet() < req.getAdet()) {
   │           return SepetEklemeResult.hata("Yetersiz stok!");
   │       }
   │       
   │       // 4. Sepete ekle
   │       Sepet sepet = new Sepet(k, urun, beden, req.getAdet());
   │       sepetRepository.save(sepet);
   │       
   │       return SepetEklemeResult.basarili("Ürün eklendi!");
   │   }
                ↓
6. REPOSITORY
   │   sepetRepository.save(sepet)
   │   → INSERT INTO sepet (kullanici_id, urun_id, beden_id, adet) VALUES (...)
                ↓
7. YANIT
   │   HTTP 200 OK
   │   {"success": true, "message": "Ürün eklendi!"}
                ↓
8. MOBİL
   │   onSuccess: () => {
   │       queryClient.invalidateQueries(['sepet']);
   │       Alert.alert('Başarılı', 'Ürün sepete eklendi!');
   │   }
```

---

## BÖLÜM 5: HATA YÖNETİMİ

### Backend Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(Map.of(
                "success", "false",
                "error", ex.getMessage()
            ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception ex) {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of(
                "success", "false",
                "error", "Sunucu hatası"
            ));
    }
}
```

### Mobil Error Handling

```typescript
try {
  await sepetApi.add(urunId, bedenId, adet);
} catch (error: any) {
  if (error.response?.status === 401) {
    // Token expired - yeniden login
    navigation.navigate('Login');
  } else if (error.response?.status === 400) {
    // Validation hatası
    Alert.alert('Hata', error.response.data.error);
  } else {
    // Network veya sunucu hatası
    Alert.alert('Hata', 'Bağlantı sorunu');
  }
}
```

---

## ÖZET

### Veri Akış Sırası

```
1. MOBİL  →  api.ts'de HTTP isteği gönderir
2. CONTROLLER  →  İsteği karşılar, Service'e yönlendirir
3. SERVICE  →  İş mantığını uygular, Repository'yi kullanır
4. REPOSITORY  →  Veritabanı sorgusu çalıştırır
5. MODEL  →  Veri yapısını tanımlar (Entity)
6. DATABASE  →  Verileri saklar (MySQL)
```

### Önemli Kurallar

1. **Controller'da iş mantığı OLMAZ** → Service'e delege et
2. **@Transactional Service katmanında kullanılır** → Veri bütünlüğü
3. **Her katman sadece altındaki katmanı kullanır** → Bağımlılık yönetimi
4. **JSON formatında iletişim** → `Content-Type: application/json`
5. **Token ile authentication** → Her istekte `Authorization: Bearer xxx`
