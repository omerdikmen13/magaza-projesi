# Mağaza Projesi - Sunum ve Senaryo Analizleri

Bu doküman, Mağaza Projesi'nin (Mobil + Backend) çalışma mantığını, katmanlar arası veri akışını ve olası hoca sorularına karşı teknik senaryoları içerir.

---

## 🏗️ 1. Mimari Genel Bakış

Projemiz **Client-Server** mimarisi kullanmaktadır:

* **İstemci (Client):** React Native (Expo) ile geliştirilmiş Mobil Uygulama.
* **Sunucu (Server):** Java Spring Boot ile geliştirilmiş REST API Backend.
* **Veritabanı (Database):** MySQL (veya MariaDB).

### İletişim Protokolü

Mobil uygulama ve Backend, **HTTP/REST** protokolü üzerinden **JSON** formatında veri alışverişi yapar.

---

## 🔄 2. Teknik Senaryolar ve Veri Akışı

Hocanın *"Bu butona basınca arkada neler oluyor?"* sorusuna verilecek teknik cevaplar.

### 📱 Senaryo 1: "Ana Sayfada Ürünlerin Listelenmesi"

Mobil uygulamada ana sayfayı açtığımızda veya aşağı kaydırdığımızda gerçekleşen akış.

**1. Mobil Katman (React Native):**

* `HomeScreen.tsx` yüklenir.
* `useEffect` içinde `apiClient.get('/urunler')` çağrısı yapılır.
* İstek `http://IP_ADRESI:8080/api/urunler` adresine gider.

**2. Controller Katmanı (Spring Boot):**

* `UrunRestController.java` sınıfındaki `@GetMapping("/api/urunler")` metodu isteği karşılar.
* Bu metod, işi `UrunService`'e devreder.

**3. Service Katmanı (Business Logic):**

* `UrunService.java` içindeki `tumUrunleriGetir()` metodu çalışır.
* Burada gerekirse filtreleme (stok var mı, aktif mi) gibi iş mantıkları uygulanır.

**4. Repository Katmanı (Data Access):**

* `UrunRepository.java` (JPA Interface) devreye girer.
* Hibernate, arka planda şu SQL'i oluşturur ve çalıştırır:
  ```sql
  SELECT * FROM urun WHERE aktif = 1;
  ```

**5. Veritabanı:**

* `urun` tablosundan kayıtlar çekilir ve Java `Urun` nesnelerine dönüştürülür.

**6. Cevap (Response):**

* Java nesneleri (List`<Urun>`) JSON formatına çevrilir.
* Mobil uygulamaya şöyle bir veri döner:
  ```json
  [
    { "id": 1, "ad": "Mavi Tişört", "fiyat": 199.99, "resimUrl": "..." },
    { "id": 2, "ad": "Koton Gömlek", "fiyat": 299.99, "resimUrl": "..." }
  ]
  ```

---

### 🛒 Senaryo 2: "Sepete Ürün Ekleme"

Kullanıcı bir ürün detayındayken "Sepete Ekle" butonuna bastığında.

**1. Mobil Katman:**

* Kullanıcı butona basar.
* API İsteği: `POST /api/sepet/ekle`
* Body (Gövde): `{ "urunId": 55, "adet": 1, "bedenId": 2 }`
* **Önemli:** Header'da `Authorization: Bearer <JWT_TOKEN>` ile kullanıcının kimliği gönderilir.

**2. Security Katmanı (Filter):**

* İstek Controller'a gelmeden önce `JwtAuthenticationFilter` araya girer.
* Token doğrulanır, kullanıcının kim olduğu (`UserContext`) belirlenir.

**3. Controller Katmanı:**

* `SepetRestController.java` -> `sepeteEkle()` metodu çalışır.
* Kullanıcı bilgisini `Authentication` nesnesinden alır.

**4. Service Katmanı (`SepetService`):**

* Önce `SepetRepository` ile kullanıcının mevcut açık bir sepeti var mı kontrol edilir. Yoksa yeni sepet oluşturulur (`sepet` tablosu).
* Sonra `SepetDetayRepository` ile bu ürün zaten sepette var mı bakılır.
  * **Varsa:** Adet artırılır (`UPDATE sepet_detay SET adet = adet + 1`).
  * **Yoksa:** Yeni satır eklenir (`INSERT INTO sepet_detay ...`).

**5. Veritabanı:**

* `sepet` ve `sepet_detay` tablolarında işlem yapılır.

**6. Cevap:**

* Başarılı ise `200 OK` ve güncel sepet özet bilgisi döner.

---

### 💳 Senaryo 3: "Sipariş Tamamlama / Ödeme"

Kullanıcı "Siparişi Tamamla" dediğinde. Bu en karmaşık işlemdir (Transaction yönetimi içerir).

**1. Mobil Katman:**

* Kullanıcı adres seçer ve ödeme butonuna basar.
* API İsteği: `POST /api/sepet/siparis-ver`

**2. Service Katmanı (`SepetService` & `SiparisService`):**

* **@Transactional** anotasyonu devreye girer. (Ya hepsi olur, ya hiçbiri).
* **Adım 1:** Sepetteki ürünlerin stokları kontrol edilir ve düşülür (`urun_stok` tablosu).
* **Adım 2:** `Sepet` nesnesi `Siparis` nesnesine dönüştürülür (`siparis` tablosuna INSERT).
* **Adım 3:** Sepet detayları `SiparisDetay` olarak kaydedilir (`siparis_detay` tablosu).
* **Adım 4:** Eski sepet durumu 'KAPANDI' yapılır veya silinir.

**3. Kritik Nokta (Hata Yönetimi):**

* Eğer stok düşülürken hata olursa (örn: stok yetersiz), `@Transactional` sayesinde tüm işlemler geri alınır (Rollback). Sipariş oluşmaz, stok düşmez.

---

### 🔐 Senaryo 4: "Mobil Giriş (Login)"

Uygulama açılırken veya Giriş Yap ekranında.

**1. Mobil Katman:**

* Kullanıcı adı ve şifre girilir.
* API İsteği: `POST /api/auth/login`
* Body: `{ "username": "ahmet", "password": "123" }`

**2. Controller & AuthenticationManager:**

* Spring Security devreye girer. Kullanıcı adı ve şifreyi `UserDetailsService` üzerinden veritabanındaki (hashlenmiş) şifre ile karşılaştırır.

**3. Token Üretimi (JWT):**

* Giriş başarılıysa, sunucu bu kullanıcı için uzun bir string olan **JWT (JSON Web Token)** üretir.
* Bu token içinde kullanıcının ID'si, Rolü ve Token'ın geçerlilik süresi şifreli olarak gizlidir.

**4. Cevap:**

* Client'a Token döner. Mobil uygulama bu token'ı hafızasında (SecureStore/AsyncStorage) saklar ve sonraki **HER** istekte sunucuya gösterir (Kimlik kartı gibi).

---

## ❓ 3. Olası Hoca Soruları ve Cevapları

**Soru 1: Ürün resimlerini nerede tutuyorsun? DB'de mi?**

* **Cevap:** Hayır hocam, veritabanını şişirmemek için resimleri veritabanında **BLOB** olarak tutmuyorum. Resimlerin sadece **URL (String)** adreslerini `urun` tablosundaki `resim_url` kolonunda tutuyorum. Resimler harici bir sunucuda (veya proje klasöründe) duruyor.

**Soru 2: Dependency Injection (Bağımlılık Enjeksiyonu) nedir, nerede kullandın?**

* **Cevap:** Spring Boot'un temel özelliğidir. `new Class()` yazmak yerine nesne yönetimini Spring'e bıraktım.
* **Örnek:** Controller içinde Service'i kullanırken `@Autowired` veya Constructor Injection (yapıcı metod) ile servisi içeri aldım. Kodda görebilirsiniz: `private final UrunService urunService;`

**Soru 3: ORM / Hibernate nedir?**

* **Cevap:** Object Relational Mapping. Java sınıfları (Entity) ile veritabanı tablolarını birbirine bğlayan teknolojidir. SQL yazmak yerine Java metodları (`findAll`, `save`) kullanarak veritabanı işlemi yapmamı sağladı.

**Soru 4: Mobilden gelen isteğin güvenliğini nasıl sağlıyorsun?**

* **Cevap:** **JWT (JSON Web Token)** yapısı kullanıyorum. Kullanıcı giriş yapınca ona bir token veriyorum. Sonraki isteklerde bu token'ı kontrol ediyorum. State-less (durumsuz) bir yapı olduğu için sunucuyu yormuyor ve mobil uyumlu.

**Soru 5: Veriler hangi formatta gidip geliyor?**

* **Cevap:** **JSON** formatında. Hem okunabilir hem de tüm platformlar (Mobil, Web) tarafından destekleniyor.

**Soru 6: DataSeeder ne işe yarıyor?**

* **Cevap:** Proje ilk ayağa kalktığında veritabanı boş oluyor. Test edebilmemiz için `CommandLineRunner` arayüzünü kullanarak başlangıç verilerini (Admin, Kategoriler, Örnek Ürünler) otomatik olarak veritabanına ekleyen sınıfım.
