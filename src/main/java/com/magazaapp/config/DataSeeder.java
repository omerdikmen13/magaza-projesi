package com.magazaapp.config;

import com.magazaapp.model.*;
import com.magazaapp.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Configuration
public class DataSeeder {

        @Bean
        CommandLineRunner initDatabase(
                        KullaniciRepository kullaniciRepository,
                        MagazaRepository magazaRepository,
                        KategoriRepository kategoriRepository,
                        AltKategoriRepository altKategoriRepository,
                        BedenRepository bedenRepository,
                        UrunRepository urunRepository,
                        UrunStokRepository urunStokRepository,
                        PasswordEncoder passwordEncoder) {

                return args -> {
                        if (kullaniciRepository.count() > 0) {
                                System.out.println(">>> Veriler zaten mevcut, DataSeeder atlanıyor...");
                                return;
                        }

                        System.out.println(">>> Başlangıç verileri ekleniyor...");

                        // KATEGORİLER
                        Kategori erkek = kategoriRepository.save(new Kategori("Erkek"));
                        Kategori kadin = kategoriRepository.save(new Kategori("Kadın"));
                        Kategori cocuk = kategoriRepository.save(new Kategori("Çocuk"));

                        // ALT KATEGORİLER
                        AltKategori erkekTisort = altKategoriRepository
                                        .save(new AltKategori("Tişört", erkek, Sezon.YAZLIK));
                        AltKategori erkekGomlek = altKategoriRepository
                                        .save(new AltKategori("Gömlek", erkek, Sezon.MEVSIMLIK));
                        AltKategori erkekKazak = altKategoriRepository
                                        .save(new AltKategori("Kazak", erkek, Sezon.KISLIK));
                        AltKategori erkekPantolon = altKategoriRepository
                                        .save(new AltKategori("Pantolon", erkek, Sezon.MEVSIMLIK));
                        AltKategori kadinElbise = altKategoriRepository
                                        .save(new AltKategori("Elbise", kadin, Sezon.YAZLIK));
                        AltKategori kadinBluz = altKategoriRepository
                                        .save(new AltKategori("Bluz", kadin, Sezon.YAZLIK));
                        AltKategori kadinEtek = altKategoriRepository
                                        .save(new AltKategori("Etek", kadin, Sezon.MEVSIMLIK));
                        AltKategori kadinKazak = altKategoriRepository
                                        .save(new AltKategori("Kazak", kadin, Sezon.KISLIK));
                        AltKategori cocukTisort = altKategoriRepository
                                        .save(new AltKategori("Tişört", cocuk, Sezon.YAZLIK));
                        AltKategori cocukSweatshirt = altKategoriRepository
                                        .save(new AltKategori("Sweatshirt", cocuk, Sezon.KISLIK));
                        AltKategori cocukPantolon = altKategoriRepository
                                        .save(new AltKategori("Pantolon", cocuk, Sezon.MEVSIMLIK));

                        // BEDENLER
                        Beden s = bedenRepository.save(new Beden("S"));
                        Beden m = bedenRepository.save(new Beden("M"));
                        Beden l = bedenRepository.save(new Beden("L"));
                        Beden xl = bedenRepository.save(new Beden("XL"));
                        List<Beden> tumBedenler = Arrays.asList(s, m, l, xl);

                        // KULLANICILAR
                        Kullanici admin = new Kullanici("admin", "admin@magaza.com", passwordEncoder.encode("admin123"),
                                        KullaniciRol.ADMIN);
                        admin.setAd("Admin");
                        admin.setSoyad("Yönetici");
                        kullaniciRepository.save(admin);

                        Kullanici maviSahip = createMagazaSahibi(kullaniciRepository, passwordEncoder, "mavi_sahip",
                                        "mavi@magaza.com", "mavi123", "Mavi", "Sahip");
                        Kullanici kotonSahip = createMagazaSahibi(kullaniciRepository, passwordEncoder, "koton_sahip",
                                        "koton@magaza.com", "koton123", "Koton", "Sahip");
                        Kullanici lcwSahip = createMagazaSahibi(kullaniciRepository, passwordEncoder, "lcw_sahip",
                                        "lcw@magaza.com",
                                        "lcw123", "LC Waikiki", "Sahip");
                        Kullanici defactoSahip = createMagazaSahibi(kullaniciRepository, passwordEncoder,
                                        "defacto_sahip",
                                        "defacto@magaza.com", "defacto123", "DeFacto", "Sahip");
                        Kullanici pullbearSahip = createMagazaSahibi(kullaniciRepository, passwordEncoder,
                                        "pullbear_sahip",
                                        "pullbear@magaza.com", "pullbear123", "Pull&Bear", "Sahip");
                        Kullanici zaraSahip = createMagazaSahibi(kullaniciRepository, passwordEncoder, "zara_sahip",
                                        "zara@magaza.com", "zara123", "Zara", "Sahip");
                        Kullanici hmSahip = createMagazaSahibi(kullaniciRepository, passwordEncoder, "hm_sahip",
                                        "hm@magaza.com",
                                        "hm123", "H&M", "Sahip");

                        // Ana mağaza sahibi - tüm ana mağazaları yönetir
                        Kullanici anaSahip = createMagazaSahibi(kullaniciRepository, passwordEncoder, "sahip12",
                                        "sahip12@magaza.com", "sahip123", "Ana", "Sahip");

                        Kullanici musteri = new Kullanici("musteri1", "musteri@test.com",
                                        passwordEncoder.encode("musteri123"),
                                        KullaniciRol.MUSTERI);
                        musteri.setAd("Test");
                        musteri.setSoyad("Müşteri");
                        musteri.setAdres("Test Mahallesi, Örnek Sokak No:1, İstanbul");
                        kullaniciRepository.save(musteri);

                        // MAĞAZALAR - Tüm ana mağazalar tek sahip tarafından yönetiliyor
                        Magaza mavi = createMagaza(magazaRepository, "Mavi", anaSahip, "Türkiye'nin denim markası",
                                        "/images/logos/Logo_of_Mavi.png");
                        Magaza koton = createMagaza(magazaRepository, "Koton", anaSahip, "Şık ve uygun fiyatlı moda",
                                        "/images/logos/koton.logo.png");
                        Magaza lcw = createMagaza(magazaRepository, "LC Waikiki", anaSahip,
                                        "Herkes için moda, herkes için kalite",
                                        "/images/logos/lcw.png");
                        Magaza defacto = createMagaza(magazaRepository, "DeFacto", anaSahip, "Trend ve konforlu giyim",
                                        "/images/logos/defacto.jpg");
                        Magaza pullbear = createMagaza(magazaRepository, "Pull&Bear", anaSahip, "Genç ve dinamik moda",
                                        "/images/logos/Pull-Bear-Logo-500x281.png");
                        Magaza zara = createMagaza(magazaRepository, "Zara", anaSahip, "Dünya çapında şık tasarımlar",
                                        "/images/logos/zara-logo.png");
                        Magaza hm = createMagaza(magazaRepository, "H&M", anaSahip, "Sürdürülebilir ve modern moda",
                                        "/images/logos/H&M-Logo.svg.png");

                        System.out.println(">>> 7 Mağaza eklendi (sahip12 yönetiyor)");

                        // ÜRÜNLER - MAVI (10 ürün)
                        createUrun(urunRepository, "Mavi Basic Tişört", "199.99", mavi, erkekTisort,
                                        "100% pamuk erkek tişört",
                                        "Mavi", "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=500");
                        createUrun(urunRepository, "Mavi V Yaka Tişört", "179.99", mavi, erkekTisort, "Slim fit V yaka",
                                        "Beyaz", "https://images.unsplash.com/photo-1581655353564-df123a1eb820?w=500");
                        createUrun(urunRepository, "Mavi Oxford Gömlek", "349.99", mavi, erkekGomlek,
                                        "Klasik oxford gömlek",
                                        "Açık Mavi",
                                        "https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=500");
                        createUrun(urunRepository, "Mavi Slim Fit Jean", "449.99", mavi, erkekPantolon,
                                        "Slim fit denim",
                                        "Koyu Mavi", "https://images.unsplash.com/photo-1542272617-08f08630329e?w=500");
                        createUrun(urunRepository, "Mavi Straight Jean", "429.99", mavi, erkekPantolon,
                                        "Rahat kesim jean",
                                        "Siyah", "https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=500");
                        createUrun(urunRepository, "Mavi Saten Bluz", "249.99", mavi, kadinBluz, "Şık saten bluz",
                                        "Bordo", "https://images.unsplash.com/photo-1564257631407-4deb1f99d992?w=500");
                        createUrun(urunRepository, "Mavi Denim Etek", "329.99", mavi, kadinEtek, "Mini denim etek",
                                        "Mavi", "https://images.unsplash.com/photo-1591189863430-ab87e120f298?w=500");
                        createUrun(urunRepository, "Mavi Yazlık Elbise", "399.99", mavi, kadinElbise,
                                        "Hafif yazlık elbise",
                                        "Beyaz", "https://images.unsplash.com/photo-1572804013309-59a88b7e92f1?w=500");
                        createUrun(urunRepository, "Mavi Çocuk Tişört", "129.99", mavi, cocukTisort,
                                        "Pamuklu çocuk tişört",
                                        "Mavi", "https://images.unsplash.com/photo-1519238809117-22b2ff150034?w=500");
                        createUrun(urunRepository, "Mavi Çocuk Sweatshirt", "199.99", mavi, cocukSweatshirt,
                                        "Kapüşonlu sweatshirt",
                                        "Gri", "https://images.unsplash.com/photo-1519238263496-d21afce7102d?w=500");

                        // ÜRÜNLER - KOTON (10 ürün)
                        createUrun(urunRepository, "Koton Baskılı Tişört", "149.99", koton, erkekTisort,
                                        "Trend baskılı tişört",
                                        "Siyah", "https://images.unsplash.com/photo-1503341504253-dff4815485f1?w=500");
                        createUrun(urunRepository, "Koton Keten Gömlek", "299.99", koton, erkekGomlek,
                                        "Yazlık keten gömlek",
                                        "Beyaz", "https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=500");
                        createUrun(urunRepository, "Koton Chino Pantolon", "349.99", koton, erkekPantolon,
                                        "Slim fit chino",
                                        "Haki", "https://images.unsplash.com/photo-1473966968600-fa801b869a1a?w=500");
                        createUrun(urunRepository, "Koton Triko Kazak", "279.99", koton, erkekKazak,
                                        "Yuvarlak yaka kazak",
                                        "Lacivert",
                                        "https://images.unsplash.com/photo-1434389677669-e08b4cac3105?w=500");
                        createUrun(urunRepository, "Koton Yazlık Elbise", "299.99", koton, kadinElbise,
                                        "Çiçek desenli elbise",
                                        "Pembe", "https://images.unsplash.com/photo-1595777457583-95e059d581b8?w=500");
                        createUrun(urunRepository, "Koton Midi Elbise", "349.99", koton, kadinElbise, "Şık midi elbise",
                                        "Yeşil", "https://images.unsplash.com/photo-1515372039744-b8f02a3ae446?w=500");
                        createUrun(urunRepository, "Koton Şifon Bluz", "179.99", koton, kadinBluz, "Hafif şifon bluz",
                                        "Beyaz", "https://images.unsplash.com/photo-1551163943-3f6a855d1153?w=500");
                        createUrun(urunRepository, "Koton Pileli Etek", "249.99", koton, kadinEtek, "Midi pileli etek",
                                        "Siyah", "https://images.unsplash.com/photo-1583496661160-fb5886a0aaaa?w=500");
                        createUrun(urunRepository, "Koton Çocuk Tişört", "99.99", koton, cocukTisort,
                                        "Renkli çocuk tişört",
                                        "Sarı", "https://images.unsplash.com/photo-1622290291468-a28f7a7dc6a8?w=500");
                        createUrun(urunRepository, "Koton Çocuk Sweatshirt", "149.99", koton, cocukSweatshirt,
                                        "Rahat sweatshirt",
                                        "Kırmızı",
                                        "https://images.unsplash.com/photo-1519238263496-d21afce7102d?w=500");

                        // ÜRÜNLER - LC WAIKIKI (10 ürün)
                        createUrun(urunRepository, "LCW Polo Tişört", "159.99", lcw, erkekTisort, "Klasik polo tişört",
                                        "Lacivert",
                                        "https://images.unsplash.com/photo-1586790170083-2f9ceadc732d?w=500");
                        createUrun(urunRepository, "LCW Kareli Gömlek", "249.99", lcw, erkekGomlek, "Kareli gömlek",
                                        "Mavi-Beyaz",
                                        "https://images.unsplash.com/photo-1598033129183-c4f50c736f10?w=500");
                        createUrun(urunRepository, "LCW Kargo Pantolon", "299.99", lcw, erkekPantolon,
                                        "Kargo cepli pantolon",
                                        "Haki", "https://images.unsplash.com/photo-1517445312882-bc9910d016b7?w=500");
                        createUrun(urunRepository, "LCW Yün Kazak", "329.99", lcw, erkekKazak, "Yün karışımlı kazak",
                                        "Gri", "https://images.unsplash.com/photo-1576566588028-4147f3842f27?w=500");
                        createUrun(urunRepository, "LCW Günlük Elbise", "279.99", lcw, kadinElbise,
                                        "Günlük rahat elbise",
                                        "Lacivert",
                                        "https://images.unsplash.com/photo-1496747611176-843222e1e57c?w=500");
                        createUrun(urunRepository, "LCW Desenli Bluz", "169.99", lcw, kadinBluz, "Çiçek desenli bluz",
                                        "Kırmızı",
                                        "https://images.unsplash.com/photo-1485462537746-965f33f7f6a7?w=500");
                        createUrun(urunRepository, "LCW Kalem Etek", "199.99", lcw, kadinEtek, "Kalem etek", "Siyah",
                                        "https://images.unsplash.com/photo-1594633312681-425c7b97ccd1?w=500");
                        createUrun(urunRepository, "LCW Boğazlı Kazak", "299.99", lcw, kadinKazak, "Boğazlı kazak",
                                        "Bej", "https://images.unsplash.com/photo-1576566588028-4147f3842f27?w=500");
                        createUrun(urunRepository, "LCW Çocuk Tişört", "89.99", lcw, cocukTisort,
                                        "Baskılı çocuk tişört", "Mavi",
                                        "https://images.unsplash.com/photo-1519238263496-d21afce7102d?w=500");
                        createUrun(urunRepository, "LCW Çocuk Pantolon", "159.99", lcw, cocukPantolon,
                                        "Rahat çocuk pantolonu",
                                        "Gri", "https://images.unsplash.com/photo-1519238263496-d21afce7102d?w=500");

                        // ÜRÜNLER - DEFACTO (10 ürün)
                        createUrun(urunRepository, "DeFacto Slim Tişört", "139.99", defacto, erkekTisort,
                                        "Slim fit tişört",
                                        "Siyah", "https://images.unsplash.com/photo-1583743814966-8936f5b7be1a?w=500");
                        createUrun(urunRepository, "DeFacto Denim Gömlek", "279.99", defacto, erkekGomlek,
                                        "Denim gömlek",
                                        "Koyu Mavi",
                                        "https://images.unsplash.com/photo-1588359348347-9bc6cbbb689e?w=500");
                        createUrun(urunRepository, "DeFacto Jogger Pantolon", "319.99", defacto, erkekPantolon,
                                        "Jogger pantolon",
                                        "Siyah", "https://images.unsplash.com/photo-1552902865-b72c031ac5ea?w=500");
                        createUrun(urunRepository, "DeFacto Fermuarlı Kazak", "349.99", defacto, erkekKazak,
                                        "Fermuarlı kazak",
                                        "Lacivert", "https://images.unsplash.com/photo-1556821840-3a63f95609a7?w=500");
                        createUrun(urunRepository, "DeFacto Maxi Elbise", "329.99", defacto, kadinElbise,
                                        "Uzun maxi elbise",
                                        "Bordo", "https://images.unsplash.com/photo-1509631179647-0177331693ae?w=500");
                        createUrun(urunRepository, "DeFacto Crop Bluz", "149.99", defacto, kadinBluz, "Crop bluz",
                                        "Beyaz", "https://images.unsplash.com/photo-1562572159-4efc207f5aff?w=500");
                        createUrun(urunRepository, "DeFacto Mini Etek", "219.99", defacto, kadinEtek, "Mini etek",
                                        "Siyah", "https://images.unsplash.com/photo-1583496661160-fb5886a0aaaa?w=500");
                        createUrun(urunRepository, "DeFacto Hırka", "279.99", defacto, kadinKazak, "Uzun hırka", "Gri",
                                        "https://images.unsplash.com/photo-1434389677669-e08b4cac3105?w=500");
                        createUrun(urunRepository, "DeFacto Çocuk Sweat", "139.99", defacto, cocukSweatshirt,
                                        "Çocuk sweatshirt",
                                        "Yeşil", "https://images.unsplash.com/photo-1519238263496-d21afce7102d?w=500");
                        createUrun(urunRepository, "DeFacto Çocuk Tişört", "79.99", defacto, cocukTisort,
                                        "Basic çocuk tişört",
                                        "Beyaz", "https://images.unsplash.com/photo-1622290291468-a28f7a7dc6a8?w=500");

                        // ÜRÜNLER - PULL&BEAR (10 ürün)
                        createUrun(urunRepository, "Pull&Bear Oversize Tişört", "169.99", pullbear, erkekTisort,
                                        "Oversize tişört",
                                        "Siyah", "https://images.unsplash.com/photo-1576566588028-4147f3842f27?w=500");
                        createUrun(urunRepository, "Pull&Bear Kısa Kollu Gömlek", "259.99", pullbear, erkekGomlek,
                                        "Yazlık gömlek",
                                        "Beyaz", "https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=500");
                        createUrun(urunRepository, "Pull&Bear Skinny Jean", "399.99", pullbear, erkekPantolon,
                                        "Skinny fit jean",
                                        "Siyah", "https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=500");
                        createUrun(urunRepository, "Pull&Bear Kapüşonlu Sweat", "329.99", pullbear, erkekKazak,
                                        "Kapüşonlu sweatshirt", "Gri",
                                        "https://images.unsplash.com/photo-1556821840-3a63f95609a7?w=500");
                        createUrun(urunRepository, "Pull&Bear Mini Elbise", "289.99", pullbear, kadinElbise,
                                        "Parti elbisesi",
                                        "Siyah", "https://images.unsplash.com/photo-1550639525-c97d455acf70?w=500");
                        createUrun(urunRepository, "Pull&Bear Crop Top", "139.99", pullbear, kadinBluz, "Crop top",
                                        "Beyaz", "https://images.unsplash.com/photo-1564257631407-4deb1f99d992?w=500");
                        createUrun(urunRepository, "Pull&Bear Denim Etek", "269.99", pullbear, kadinEtek, "Denim etek",
                                        "Mavi", "https://images.unsplash.com/photo-1591189863430-ab87e120f298?w=500");
                        createUrun(urunRepository, "Pull&Bear Triko", "299.99", pullbear, kadinKazak, "Triko kazak",
                                        "Bej", "https://images.unsplash.com/photo-1576566588028-4147f3842f27?w=500");
                        createUrun(urunRepository, "Pull&Bear Çocuk Tişört", "119.99", pullbear, cocukTisort,
                                        "Genç tişört",
                                        "Siyah", "https://images.unsplash.com/photo-1519238263496-d21afce7102d?w=500");
                        createUrun(urunRepository, "Pull&Bear Çocuk Sweat", "189.99", pullbear, cocukSweatshirt,
                                        "Genç sweatshirt",
                                        "Lacivert",
                                        "https://images.unsplash.com/photo-1519238263496-d21afce7102d?w=500");

                        // ÜRÜNLER - ZARA (10 ürün)
                        createUrun(urunRepository, "Zara Premium Tişört", "229.99", zara, erkekTisort,
                                        "Premium pamuk tişört",
                                        "Beyaz", "https://images.unsplash.com/photo-1581655353564-df123a1eb820?w=500");
                        createUrun(urunRepository, "Zara Saten Gömlek", "449.99", zara, erkekGomlek, "Saten gömlek",
                                        "Siyah", "https://images.unsplash.com/photo-1607345366928-199ea26cfe3e?w=500");
                        createUrun(urunRepository, "Zara Kumaş Pantolon", "529.99", zara, erkekPantolon,
                                        "Kumaş pantolon",
                                        "Lacivert",
                                        "https://images.unsplash.com/photo-1594938298603-c8148c4dae35?w=500");
                        createUrun(urunRepository, "Zara Yün Kazak", "599.99", zara, erkekKazak, "Yün kazak", "Gri",
                                        "https://images.unsplash.com/photo-1434389677669-e08b4cac3105?w=500");
                        createUrun(urunRepository, "Zara Kokteyl Elbise", "699.99", zara, kadinElbise,
                                        "Şık kokteyl elbise",
                                        "Kırmızı",
                                        "https://images.unsplash.com/photo-1566174053879-31528523f8ae?w=500");
                        createUrun(urunRepository, "Zara İpek Bluz", "399.99", zara, kadinBluz, "İpek bluz", "Beyaz",
                                        "https://images.unsplash.com/photo-1564257631407-4deb1f99d992?w=500");
                        createUrun(urunRepository, "Zara Kalem Etek", "349.99", zara, kadinEtek, "Kalem etek", "Siyah",
                                        "https://images.unsplash.com/photo-1594633312681-425c7b97ccd1?w=500");
                        createUrun(urunRepository, "Zara Kaşmir Kazak", "799.99", zara, kadinKazak, "Kaşmir kazak",
                                        "Bej", "https://images.unsplash.com/photo-1576566588028-4147f3842f27?w=500");
                        createUrun(urunRepository, "Zara Çocuk Gömlek", "199.99", zara, erkekGomlek, "Çocuk gömlek",
                                        "Mavi", "https://images.unsplash.com/photo-1519238263496-d21afce7102d?w=500");
                        createUrun(urunRepository, "Zara Çocuk Pantolon", "249.99", zara, cocukPantolon,
                                        "Çocuk kumaş pantolon",
                                        "Lacivert",
                                        "https://images.unsplash.com/photo-1519238263496-d21afce7102d?w=500");

                        // ÜRÜNLER - H&M (10 ürün)
                        createUrun(urunRepository, "H&M Basic Tişört", "99.99", hm, erkekTisort, "Basic tişört",
                                        "Beyaz", "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=500");
                        createUrun(urunRepository, "H&M Pamuk Gömlek", "229.99", hm, erkekGomlek, "Pamuklu gömlek",
                                        "Mavi", "https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=500");
                        createUrun(urunRepository, "H&M Slim Pantolon", "349.99", hm, erkekPantolon,
                                        "Slim fit pantolon", "Siyah",
                                        "https://images.unsplash.com/photo-1542272617-08f08630329e?w=500");
                        createUrun(urunRepository, "H&M Triko Kazak", "279.99", hm, erkekKazak, "Triko kazak", "Gri",
                                        "https://images.unsplash.com/photo-1434389677669-e08b4cac3105?w=500");
                        createUrun(urunRepository, "H&M Günlük Elbise", "299.99", hm, kadinElbise, "Günlük elbise",
                                        "Yeşil", "https://images.unsplash.com/photo-1496747611176-843222e1e57c?w=500");
                        createUrun(urunRepository, "H&M Pamuk Bluz", "149.99", hm, kadinBluz, "Pamuklu bluz", "Beyaz",
                                        "https://images.unsplash.com/photo-1564257631407-4deb1f99d992?w=500");
                        createUrun(urunRepository, "H&M Midi Etek", "229.99", hm, kadinEtek, "Midi etek", "Siyah",
                                        "https://images.unsplash.com/photo-1583496661160-fb5886a0aaaa?w=500");
                        createUrun(urunRepository, "H&M Hırka", "249.99", hm, kadinKazak, "Uzun hırka", "Bej",
                                        "https://images.unsplash.com/photo-1576566588028-4147f3842f27?w=500");
                        createUrun(urunRepository, "H&M Çocuk Tişört", "79.99", hm, cocukTisort, "Organik pamuk tişört",
                                        "Yeşil", "https://images.unsplash.com/photo-1622290291468-a28f7a7dc6a8?w=500");
                        createUrun(urunRepository, "H&M Çocuk Sweat", "159.99", hm, cocukSweatshirt, "Çocuk sweatshirt",
                                        "Gri", "https://images.unsplash.com/photo-1519238263496-d21afce7102d?w=500");

                        // ==================== EK ÜRÜNLER (Her mağazadan 20 ürün daha)
                        // ====================

                        // MAVI EK ÜRÜNLER
                        createUrun(urunRepository, "Mavi Oversize Tişört", "219.99", mavi, erkekTisort,
                                        "Oversize pamuk tişört",
                                        "Siyah", "https://images.unsplash.com/photo-1576566588028-4147f3842f27?w=500");
                        createUrun(urunRepository, "Mavi Baskılı Tişört", "209.99", mavi, erkekTisort,
                                        "Baskılı günlük tişört",
                                        "Beyaz", "https://images.unsplash.com/photo-1503341504253-dff4815485f1?w=500");
                        createUrun(urunRepository, "Mavi Keten Gömlek", "389.99", mavi, erkekGomlek,
                                        "Yazlık keten gömlek", "Bej",
                                        "https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=500");
                        createUrun(urunRepository, "Mavi Çizgili Gömlek", "369.99", mavi, erkekGomlek,
                                        "Çizgili klasik gömlek",
                                        "Mavi-Beyaz",
                                        "https://images.unsplash.com/photo-1598033129183-c4f50c736f10?w=500");
                        createUrun(urunRepository, "Mavi Triko Kazak", "319.99", mavi, erkekKazak, "Triko kazak",
                                        "Lacivert",
                                        "https://images.unsplash.com/photo-1434389677669-e08b4cac3105?w=500");
                        createUrun(urunRepository, "Mavi Boğazlı Kazak", "339.99", mavi, erkekKazak,
                                        "Boğazlı kışlık kazak", "Gri",
                                        "https://images.unsplash.com/photo-1576566588028-4147f3842f27?w=500");
                        createUrun(urunRepository, "Mavi Kargo Pantolon", "469.99", mavi, erkekPantolon,
                                        "Kargo cepli pantolon",
                                        "Haki", "https://images.unsplash.com/photo-1517445312882-bc9910d016b7?w=500");
                        createUrun(urunRepository, "Mavi Jogger Pantolon", "449.99", mavi, erkekPantolon,
                                        "Rahat jogger pantolon",
                                        "Siyah", "https://images.unsplash.com/photo-1552902865-b72c031ac5ea?w=500");
                        createUrun(urunRepository, "Mavi Günlük Elbise", "429.99", mavi, kadinElbise,
                                        "Günlük rahat elbise",
                                        "Yeşil", "https://images.unsplash.com/photo-1496747611176-843222e1e57c?w=500");
                        createUrun(urunRepository, "Mavi Çiçekli Elbise", "459.99", mavi, kadinElbise,
                                        "Çiçek desenli elbise",
                                        "Pembe", "https://images.unsplash.com/photo-1595777457583-95e059d581b8?w=500");
                        createUrun(urunRepository, "Mavi Pamuk Bluz", "229.99", mavi, kadinBluz, "Pamuklu bluz",
                                        "Beyaz", "https://images.unsplash.com/photo-1564257631407-4deb1f99d992?w=500");
                        createUrun(urunRepository, "Mavi Desenli Bluz", "239.99", mavi, kadinBluz, "Desenli şık bluz",
                                        "Bordo", "https://images.unsplash.com/photo-1551163943-3f6a855d1153?w=500");
                        createUrun(urunRepository, "Mavi Midi Etek", "339.99", mavi, kadinEtek, "Midi etek", "Siyah",
                                        "https://images.unsplash.com/photo-1583496661160-fb5886a0aaaa?w=500");
                        createUrun(urunRepository, "Mavi Pileli Etek", "349.99", mavi, kadinEtek, "Pileli etek", "Bej",
                                        "https://images.unsplash.com/photo-1591189863430-ab87e120f298?w=500");
                        createUrun(urunRepository, "Mavi Kadın Triko Kazak", "379.99", mavi, kadinKazak,
                                        "Kadın triko kazak",
                                        "Krem", "https://images.unsplash.com/photo-1576566588028-4147f3842f27?w=500");
                        createUrun(urunRepository, "Mavi Kadın Boğazlı Kazak", "399.99", mavi, kadinKazak,
                                        "Kadın boğazlı kazak",
                                        "Gri", "https://images.unsplash.com/photo-1434389677669-e08b4cac3105?w=500");
                        createUrun(urunRepository, "Mavi Çocuk Pantolon", "169.99", mavi, cocukPantolon,
                                        "Rahat çocuk pantolonu",
                                        "Gri", "https://images.unsplash.com/photo-1519238263496-d21afce7102d?w=500");
                        createUrun(urunRepository, "Mavi Çocuk Jean", "189.99", mavi, cocukPantolon,
                                        "Çocuk denim pantolon",
                                        "Mavi", "https://images.unsplash.com/photo-1519238263496-d21afce7102d?w=500");
                        createUrun(urunRepository, "Mavi Çocuk Baskılı Tişört", "139.99", mavi, cocukTisort,
                                        "Baskılı çocuk tişört",
                                        "Sarı", "https://images.unsplash.com/photo-1622290291468-a28f7a7dc6a8?w=500");
                        createUrun(urunRepository, "Mavi Çocuk Fermuarlı Sweatshirt", "219.99", mavi, cocukSweatshirt,
                                        "Fermuarlı sweatshirt", "Lacivert",
                                        "https://images.unsplash.com/photo-1519238263496-d21afce7102d?w=500");

                        // KOTON EK ÜRÜNLER
                        createUrun(urunRepository, "Koton Oversize Tişört", "159.99", koton, erkekTisort,
                                        "Oversize tişört",
                                        "Beyaz", "https://images.unsplash.com/photo-1576566588028-4147f3842f27?w=500");
                        createUrun(urunRepository, "Koton Basic Tişört", "139.99", koton, erkekTisort,
                                        "Basic pamuk tişört",
                                        "Siyah", "https://images.unsplash.com/photo-1583743814966-8936f5b7be1a?w=500");
                        createUrun(urunRepository, "Koton Oxford Gömlek", "319.99", koton, erkekGomlek, "Oxford gömlek",
                                        "Açık Mavi",
                                        "https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=500");
                        createUrun(urunRepository, "Koton Kısa Kollu Gömlek", "279.99", koton, erkekGomlek,
                                        "Yazlık kısa kollu gömlek", "Bej",
                                        "https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=500");
                        createUrun(urunRepository, "Koton Boğazlı Kazak", "299.99", koton, erkekKazak, "Boğazlı kazak",
                                        "Gri", "https://images.unsplash.com/photo-1434389677669-e08b4cac3105?w=500");
                        createUrun(urunRepository, "Koton Triko Kazak", "289.99", koton, erkekKazak, "Triko kazak",
                                        "Lacivert",
                                        "https://images.unsplash.com/photo-1576566588028-4147f3842f27?w=500");
                        createUrun(urunRepository, "Koton Slim Pantolon", "369.99", koton, erkekPantolon,
                                        "Slim fit pantolon",
                                        "Siyah", "https://images.unsplash.com/photo-1542272617-08f08630329e?w=500");
                        createUrun(urunRepository, "Koton Kargo Pantolon", "389.99", koton, erkekPantolon,
                                        "Kargo cepli pantolon",
                                        "Haki", "https://images.unsplash.com/photo-1517445312882-bc9910d016b7?w=500");
                        createUrun(urunRepository, "Koton Günlük Elbise", "319.99", koton, kadinElbise, "Günlük elbise",
                                        "Lacivert",
                                        "https://images.unsplash.com/photo-1496747611176-843222e1e57c?w=500");
                        createUrun(urunRepository, "Koton Saten Elbise", "399.99", koton, kadinElbise, "Saten elbise",
                                        "Siyah", "https://images.unsplash.com/photo-1509631179647-0177331693ae?w=500");
                        createUrun(urunRepository, "Koton V Yaka Bluz", "189.99", koton, kadinBluz, "V yaka bluz",
                                        "Beyaz", "https://images.unsplash.com/photo-1564257631407-4deb1f99d992?w=500");
                        createUrun(urunRepository, "Koton Desenli Bluz", "199.99", koton, kadinBluz, "Desenli bluz",
                                        "Kırmızı", "https://images.unsplash.com/photo-1551163943-3f6a855d1153?w=500");
                        createUrun(urunRepository, "Koton Midi Etek", "269.99", koton, kadinEtek, "Midi etek", "Bej",
                                        "https://images.unsplash.com/photo-1583496661160-fb5886a0aaaa?w=500");
                        createUrun(urunRepository, "Koton Kot Etek", "289.99", koton, kadinEtek, "Denim etek", "Mavi",
                                        "https://images.unsplash.com/photo-1591189863430-ab87e120f298?w=500");
                        createUrun(urunRepository, "Koton Kadın Triko Kazak", "319.99", koton, kadinKazak,
                                        "Kadın triko kazak",
                                        "Gri", "https://images.unsplash.com/photo-1576566588028-4147f3842f27?w=500");
                        createUrun(urunRepository, "Koton Kadın Boğazlı Kazak", "339.99", koton, kadinKazak,
                                        "Boğazlı kazak",
                                        "Siyah", "https://images.unsplash.com/photo-1434389677669-e08b4cac3105?w=500");
                        createUrun(urunRepository, "Koton Çocuk Pantolon", "149.99", koton, cocukPantolon,
                                        "Çocuk pantolon", "Gri",
                                        "https://images.unsplash.com/photo-1519238263496-d21afce7102d?w=500");
                        createUrun(urunRepository, "Koton Çocuk Jean", "169.99", koton, cocukPantolon, "Çocuk jean",
                                        "Mavi", "https://images.unsplash.com/photo-1519238263496-d21afce7102d?w=500");
                        createUrun(urunRepository, "Koton Çocuk Baskılı Tişört", "109.99", koton, cocukTisort,
                                        "Baskılı çocuk tişört", "Sarı",
                                        "https://images.unsplash.com/photo-1622290291468-a28f7a7dc6a8?w=500");
                        createUrun(urunRepository, "Koton Çocuk Fermuarlı Sweatshirt", "169.99", koton, cocukSweatshirt,
                                        "Fermuarlı sweatshirt", "Lacivert",
                                        "https://images.unsplash.com/photo-1519238263496-d21afce7102d?w=500");

                        // LCW EK ÜRÜNLER
                        createUrun(urunRepository, "LCW Basic Tişört", "119.99", lcw, erkekTisort, "Basic tişört",
                                        "Beyaz", "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=500");
                        createUrun(urunRepository, "LCW Baskılı Tişört", "129.99", lcw, erkekTisort, "Baskılı tişört",
                                        "Siyah", "https://images.unsplash.com/photo-1503341504253-dff4815485f1?w=500");
                        createUrun(urunRepository, "LCW Oxford Gömlek", "269.99", lcw, erkekGomlek, "Oxford gömlek",
                                        "Açık Mavi",
                                        "https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=500");
                        createUrun(urunRepository, "LCW Keten Gömlek", "289.99", lcw, erkekGomlek, "Keten gömlek",
                                        "Bej", "https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=500");
                        createUrun(urunRepository, "LCW Triko Kazak", "299.99", lcw, erkekKazak, "Triko kazak", "Gri",
                                        "https://images.unsplash.com/photo-1434389677669-e08b4cac3105?w=500");
                        createUrun(urunRepository, "LCW Boğazlı Kazak", "319.99", lcw, erkekKazak, "Boğazlı kazak",
                                        "Lacivert",
                                        "https://images.unsplash.com/photo-1576566588028-4147f3842f27?w=500");
                        createUrun(urunRepository, "LCW Slim Pantolon", "329.99", lcw, erkekPantolon, "Slim pantolon",
                                        "Siyah", "https://images.unsplash.com/photo-1542272617-08f08630329e?w=500");
                        createUrun(urunRepository, "LCW Kargo Pantolon", "339.99", lcw, erkekPantolon, "Kargo pantolon",
                                        "Haki", "https://images.unsplash.com/photo-1517445312882-bc9910d016b7?w=500");
                        createUrun(urunRepository, "LCW Günlük Elbise", "299.99", lcw, kadinElbise, "Günlük elbise",
                                        "Yeşil", "https://images.unsplash.com/photo-1496747611176-843222e1e57c?w=500");
                        createUrun(urunRepository, "LCW Desenli Elbise", "319.99", lcw, kadinElbise, "Desenli elbise",
                                        "Pembe", "https://images.unsplash.com/photo-1572804013309-59a88b7e92f1?w=500");
                        createUrun(urunRepository, "LCW Pamuk Bluz", "159.99", lcw, kadinBluz, "Pamuk bluz", "Beyaz",
                                        "https://images.unsplash.com/photo-1564257631407-4deb1f99d992?w=500");
                        createUrun(urunRepository, "LCW Şifon Bluz", "179.99", lcw, kadinBluz, "Şifon bluz", "Bordo",
                                        "https://images.unsplash.com/photo-1551163943-3f6a855d1153?w=500");
                        createUrun(urunRepository, "LCW Midi Etek", "219.99", lcw, kadinEtek, "Midi etek", "Siyah",
                                        "https://images.unsplash.com/photo-1583496661160-fb5886a0aaaa?w=500");
                        createUrun(urunRepository, "LCW Pileli Etek", "239.99", lcw, kadinEtek, "Pileli etek", "Bej",
                                        "https://images.unsplash.com/photo-1591189863430-ab87e120f298?w=500");
                        createUrun(urunRepository, "LCW Kadın Triko Kazak", "279.99", lcw, kadinKazak, "Triko kazak",
                                        "Gri", "https://images.unsplash.com/photo-1576566588028-4147f3842f27?w=500");
                        createUrun(urunRepository, "LCW Kadın Boğazlı Kazak", "299.99", lcw, kadinKazak,
                                        "Boğazlı kazak",
                                        "Lacivert",
                                        "https://images.unsplash.com/photo-1434389677669-e08b4cac3105?w=500");
                        createUrun(urunRepository, "LCW Çocuk Pantolon", "149.99", lcw, cocukPantolon, "Çocuk pantolon",
                                        "Gri", "https://images.unsplash.com/photo-1519238263496-d21afce7102d?w=500");
                        createUrun(urunRepository, "LCW Çocuk Jean", "169.99", lcw, cocukPantolon, "Çocuk jean",
                                        "Mavi", "https://images.unsplash.com/photo-1622290291468-a28f7a7dc6a8?w=500");
                        createUrun(urunRepository, "LCW Çocuk Baskılı Tişört", "99.99", lcw, cocukTisort,
                                        "Baskılı tişört", "Sarı",
                                        "https://images.unsplash.com/photo-1519238809117-22b2ff150034?w=500");
                        createUrun(urunRepository, "LCW Çocuk Fermuarlı Sweatshirt", "179.99", lcw, cocukSweatshirt,
                                        "Fermuarlı sweatshirt", "Lacivert",
                                        "https://images.unsplash.com/photo-1519238263496-d21afce7102d?w=500");

                        // DEFACTO EK ÜRÜNLER
                        createUrun(urunRepository, "DeFacto Basic Tişört", "129.99", defacto, erkekTisort,
                                        "Basic pamuk tişört",
                                        "Beyaz", "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=500");
                        createUrun(urunRepository, "DeFacto Oversize Tişört", "149.99", defacto, erkekTisort,
                                        "Oversize tişört",
                                        "Siyah", "https://images.unsplash.com/photo-1576566588028-4147f3842f27?w=500");
                        createUrun(urunRepository, "DeFacto Oxford Gömlek", "299.99", defacto, erkekGomlek,
                                        "Oxford gömlek",
                                        "Açık Mavi",
                                        "https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=500");
                        createUrun(urunRepository, "DeFacto Keten Gömlek", "319.99", defacto, erkekGomlek,
                                        "Keten gömlek", "Bej",
                                        "https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=500");
                        createUrun(urunRepository, "DeFacto Triko Kazak", "319.99", defacto, erkekKazak, "Triko kazak",
                                        "Gri", "https://images.unsplash.com/photo-1434389677669-e08b4cac3105?w=500");
                        createUrun(urunRepository, "DeFacto Boğazlı Kazak", "339.99", defacto, erkekKazak,
                                        "Boğazlı kazak",
                                        "Lacivert",
                                        "https://images.unsplash.com/photo-1576566588028-4147f3842f27?w=500");
                        createUrun(urunRepository, "DeFacto Slim Pantolon", "349.99", defacto, erkekPantolon,
                                        "Slim pantolon",
                                        "Siyah", "https://images.unsplash.com/photo-1542272617-08f08630329e?w=500");
                        createUrun(urunRepository, "DeFacto Kargo Pantolon", "369.99", defacto, erkekPantolon,
                                        "Kargo pantolon",
                                        "Haki", "https://images.unsplash.com/photo-1517445312882-bc9910d016b7?w=500");
                        createUrun(urunRepository, "DeFacto Günlük Elbise", "309.99", defacto, kadinElbise,
                                        "Günlük elbise",
                                        "Yeşil", "https://images.unsplash.com/photo-1496747611176-843222e1e57c?w=500");
                        createUrun(urunRepository, "DeFacto Saten Elbise", "359.99", defacto, kadinElbise,
                                        "Saten elbise", "Siyah",
                                        "https://images.unsplash.com/photo-1509631179647-0177331693ae?w=500");
                        createUrun(urunRepository, "DeFacto Pamuk Bluz", "159.99", defacto, kadinBluz, "Pamuk bluz",
                                        "Beyaz", "https://images.unsplash.com/photo-1564257631407-4deb1f99d992?w=500");
                        createUrun(urunRepository, "DeFacto Şifon Bluz", "179.99", defacto, kadinBluz, "Şifon bluz",
                                        "Bordo", "https://images.unsplash.com/photo-1551163943-3f6a855d1153?w=500");
                        createUrun(urunRepository, "DeFacto Midi Etek", "239.99", defacto, kadinEtek, "Midi etek",
                                        "Siyah", "https://images.unsplash.com/photo-1583496661160-fb5886a0aaaa?w=500");
                        createUrun(urunRepository, "DeFacto Denim Etek", "259.99", defacto, kadinEtek, "Denim etek",
                                        "Mavi", "https://images.unsplash.com/photo-1591189863430-ab87e120f298?w=500");
                        createUrun(urunRepository, "DeFacto Kadın Triko Kazak", "299.99", defacto, kadinKazak,
                                        "Triko kazak",
                                        "Gri", "https://images.unsplash.com/photo-1576566588028-4147f3842f27?w=500");
                        createUrun(urunRepository, "DeFacto Kadın Boğazlı Kazak", "329.99", defacto, kadinKazak,
                                        "Boğazlı kazak",
                                        "Lacivert",
                                        "https://images.unsplash.com/photo-1434389677669-e08b4cac3105?w=500");
                        createUrun(urunRepository, "DeFacto Çocuk Pantolon", "149.99", defacto, cocukPantolon,
                                        "Çocuk pantolon",
                                        "Gri", "https://images.unsplash.com/photo-1519238263496-d21afce7102d?w=500");
                        createUrun(urunRepository, "DeFacto Çocuk Jean", "169.99", defacto, cocukPantolon, "Çocuk jean",
                                        "Mavi", "https://images.unsplash.com/photo-1622290291468-a28f7a7dc6a8?w=500");
                        createUrun(urunRepository, "DeFacto Çocuk Baskılı Tişört", "89.99", defacto, cocukTisort,
                                        "Baskılı tişört",
                                        "Sarı", "https://images.unsplash.com/photo-1519238809117-22b2ff150034?w=500");
                        createUrun(urunRepository, "DeFacto Çocuk Fermuarlı Sweatshirt", "179.99", defacto,
                                        cocukSweatshirt,
                                        "Fermuarlı sweatshirt", "Lacivert",
                                        "https://images.unsplash.com/photo-1519238263496-d21afce7102d?w=500");

                        // PULL&BEAR EK ÜRÜNLER
                        createUrun(urunRepository, "Pull&Bear Basic Tişört", "159.99", pullbear, erkekTisort,
                                        "Basic tişört",
                                        "Beyaz", "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=500");
                        createUrun(urunRepository, "Pull&Bear Baskılı Tişört", "179.99", pullbear, erkekTisort,
                                        "Baskılı tişört",
                                        "Siyah", "https://images.unsplash.com/photo-1503341504253-dff4815485f1?w=500");
                        createUrun(urunRepository, "Pull&Bear Oxford Gömlek", "289.99", pullbear, erkekGomlek,
                                        "Oxford gömlek",
                                        "Açık Mavi",
                                        "https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=500");
                        createUrun(urunRepository, "Pull&Bear Keten Gömlek", "309.99", pullbear, erkekGomlek,
                                        "Keten gömlek",
                                        "Bej", "https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=500");
                        createUrun(urunRepository, "Pull&Bear Triko Kazak", "329.99", pullbear, erkekKazak,
                                        "Triko kazak", "Gri",
                                        "https://images.unsplash.com/photo-1434389677669-e08b4cac3105?w=500");
                        createUrun(urunRepository, "Pull&Bear Boğazlı Kazak", "349.99", pullbear, erkekKazak,
                                        "Boğazlı kazak",
                                        "Lacivert",
                                        "https://images.unsplash.com/photo-1576566588028-4147f3842f27?w=500");
                        createUrun(urunRepository, "Pull&Bear Slim Pantolon", "399.99", pullbear, erkekPantolon,
                                        "Slim pantolon",
                                        "Siyah", "https://images.unsplash.com/photo-1542272617-08f08630329e?w=500");
                        createUrun(urunRepository, "Pull&Bear Kargo Pantolon", "419.99", pullbear, erkekPantolon,
                                        "Kargo pantolon",
                                        "Haki", "https://images.unsplash.com/photo-1517445312882-bc9910d016b7?w=500");
                        createUrun(urunRepository, "Pull&Bear Günlük Elbise", "309.99", pullbear, kadinElbise,
                                        "Günlük elbise",
                                        "Yeşil", "https://images.unsplash.com/photo-1496747611176-843222e1e57c?w=500");
                        createUrun(urunRepository, "Pull&Bear Parti Elbisesi", "349.99", pullbear, kadinElbise,
                                        "Şık parti elbisesi", "Siyah",
                                        "https://images.unsplash.com/photo-1595777457583-95e059d581b8?w=500");
                        createUrun(urunRepository, "Pull&Bear Pamuk Bluz", "149.99", pullbear, kadinBluz, "Pamuk bluz",
                                        "Beyaz", "https://images.unsplash.com/photo-1564257631407-4deb1f99d992?w=500");
                        createUrun(urunRepository, "Pull&Bear Crop Bluz", "159.99", pullbear, kadinBluz, "Crop bluz",
                                        "Bordo", "https://images.unsplash.com/photo-1551163943-3f6a855d1153?w=500");
                        createUrun(urunRepository, "Pull&Bear Midi Etek", "259.99", pullbear, kadinEtek, "Midi etek",
                                        "Siyah", "https://images.unsplash.com/photo-1583496661160-fb5886a0aaaa?w=500");
                        createUrun(urunRepository, "Pull&Bear Denim Etek", "279.99", pullbear, kadinEtek, "Denim etek",
                                        "Mavi", "https://images.unsplash.com/photo-1591189863430-ab87e120f298?w=500");
                        createUrun(urunRepository, "Pull&Bear Kadın Triko Kazak", "319.99", pullbear, kadinKazak,
                                        "Triko kazak",
                                        "Bej", "https://images.unsplash.com/photo-1576566588028-4147f3842f27?w=500");
                        createUrun(urunRepository, "Pull&Bear Kadın Boğazlı Kazak", "339.99", pullbear, kadinKazak,
                                        "Boğazlı kazak",
                                        "Gri", "https://images.unsplash.com/photo-1434389677669-e08b4cac3105?w=500");
                        createUrun(urunRepository, "Pull&Bear Çocuk Pantolon", "179.99", pullbear, cocukPantolon,
                                        "Çocuk pantolon",
                                        "Gri", "https://images.unsplash.com/photo-1519238263496-d21afce7102d?w=500");
                        createUrun(urunRepository, "Pull&Bear Çocuk Jean", "199.99", pullbear, cocukPantolon,
                                        "Çocuk jean", "Mavi",
                                        "https://images.unsplash.com/photo-1622290291468-a28f7a7dc6a8?w=500");
                        createUrun(urunRepository, "Pull&Bear Çocuk Baskılı Tişört", "129.99", pullbear, cocukTisort,
                                        "Baskılı tişört", "Sarı",
                                        "https://images.unsplash.com/photo-1519238809117-22b2ff150034?w=500");
                        createUrun(urunRepository, "Pull&Bear Çocuk Fermuarlı Sweatshirt", "209.99", pullbear,
                                        cocukSweatshirt,
                                        "Fermuarlı sweatshirt", "Lacivert",
                                        "https://images.unsplash.com/photo-1519238263496-d21afce7102d?w=500");

                        // ZARA EK ÜRÜNLER
                        createUrun(urunRepository, "Zara Basic Tişört", "249.99", zara, erkekTisort,
                                        "Basic premium tişört",
                                        "Beyaz", "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=500");
                        createUrun(urunRepository, "Zara Baskılı Tişört", "269.99", zara, erkekTisort,
                                        "Baskılı premium tişört",
                                        "Siyah", "https://images.unsplash.com/photo-1503341504253-dff4815485f1?w=500");
                        createUrun(urunRepository, "Zara Oxford Gömlek", "479.99", zara, erkekGomlek, "Oxford gömlek",
                                        "Açık Mavi",
                                        "https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=500");
                        createUrun(urunRepository, "Zara Keten Gömlek", "499.99", zara, erkekGomlek, "Keten gömlek",
                                        "Bej", "https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=500");
                        createUrun(urunRepository, "Zara Triko Kazak", "629.99", zara, erkekKazak, "Triko kazak",
                                        "Gri", "https://images.unsplash.com/photo-1434389677669-e08b4cac3105?w=500");
                        createUrun(urunRepository, "Zara Boğazlı Kazak", "649.99", zara, erkekKazak, "Boğazlı kazak",
                                        "Lacivert",
                                        "https://images.unsplash.com/photo-1576566588028-4147f3842f27?w=500");
                        createUrun(urunRepository, "Zara Slim Pantolon", "549.99", zara, erkekPantolon,
                                        "Slim kumaş pantolon",
                                        "Siyah", "https://images.unsplash.com/photo-1542272617-08f08630329e?w=500");
                        createUrun(urunRepository, "Zara Kargo Pantolon", "569.99", zara, erkekPantolon,
                                        "Kargo pantolon", "Haki",
                                        "https://images.unsplash.com/photo-1517445312882-bc9910d016b7?w=500");
                        createUrun(urunRepository, "Zara Günlük Elbise", "629.99", zara, kadinElbise, "Günlük elbise",
                                        "Yeşil", "https://images.unsplash.com/photo-1496747611176-843222e1e57c?w=500");
                        createUrun(urunRepository, "Zara Saten Elbise", "699.99", zara, kadinElbise, "Saten elbise",
                                        "Siyah", "https://images.unsplash.com/photo-1509631179647-0177331693ae?w=500");
                        createUrun(urunRepository, "Zara Pamuk Bluz", "429.99", zara, kadinBluz, "Pamuklu bluz",
                                        "Beyaz", "https://images.unsplash.com/photo-1564257631407-4deb1f99d992?w=500");
                        createUrun(urunRepository, "Zara İpek Bluz", "499.99", zara, kadinBluz, "İpek bluz", "Bordo",
                                        "https://images.unsplash.com/photo-1564257631407-4deb1f99d992?w=500");
                        createUrun(urunRepository, "Zara Midi Etek", "399.99", zara, kadinEtek, "Midi etek", "Siyah",
                                        "https://images.unsplash.com/photo-1583496661160-fb5886a0aaaa?w=500");
                        createUrun(urunRepository, "Zara Denim Etek", "419.99", zara, kadinEtek, "Denim etek", "Mavi",
                                        "https://images.unsplash.com/photo-1591189863430-ab87e120f298?w=500");
                        createUrun(urunRepository, "Zara Kadın Triko Kazak", "699.99", zara, kadinKazak, "Triko kazak",
                                        "Bej", "https://images.unsplash.com/photo-1576566588028-4147f3842f27?w=500");
                        createUrun(urunRepository, "Zara Kaşmir Kazak", "899.99", zara, kadinKazak, "Kaşmir kazak",
                                        "Gri", "https://images.unsplash.com/photo-1576566588028-4147f3842f27?w=500");
                        createUrun(urunRepository, "Zara Çocuk Pantolon", "269.99", zara, cocukPantolon,
                                        "Çocuk pantolon", "Gri",
                                        "https://images.unsplash.com/photo-1519238263496-d21afce7102d?w=500");
                        createUrun(urunRepository, "Zara Çocuk Jean", "299.99", zara, cocukPantolon, "Çocuk jean",
                                        "Mavi", "https://images.unsplash.com/photo-1622290291468-a28f7a7dc6a8?w=500");
                        createUrun(urunRepository, "Zara Çocuk Baskılı Tişört", "199.99", zara, cocukTisort,
                                        "Baskılı tişört",
                                        "Sarı", "https://images.unsplash.com/photo-1519238809117-22b2ff150034?w=500");
                        createUrun(urunRepository, "Zara Çocuk Fermuarlı Sweatshirt", "279.99", zara, cocukSweatshirt,
                                        "Fermuarlı sweatshirt", "Lacivert",
                                        "https://images.unsplash.com/photo-1519238263496-d21afce7102d?w=500");

                        // H&M EK ÜRÜNLER
                        createUrun(urunRepository, "H&M Oversize Tişört", "129.99", hm, erkekTisort, "Oversize tişört",
                                        "Siyah", "https://images.unsplash.com/photo-1576566588028-4147f3842f27?w=500");
                        createUrun(urunRepository, "H&M Baskılı Tişört", "119.99", hm, erkekTisort, "Baskılı tişört",
                                        "Beyaz", "https://images.unsplash.com/photo-1503341504253-dff4815485f1?w=500");
                        createUrun(urunRepository, "H&M Oxford Gömlek", "249.99", hm, erkekGomlek, "Oxford gömlek",
                                        "Açık Mavi",
                                        "https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=500");
                        createUrun(urunRepository, "H&M Keten Gömlek", "269.99", hm, erkekGomlek, "Keten gömlek",
                                        "Bej", "https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=500");
                        createUrun(urunRepository, "H&M Boğazlı Kazak", "299.99", hm, erkekKazak, "Boğazlı kazak",
                                        "Gri", "https://images.unsplash.com/photo-1576566588028-4147f3842f27?w=500");
                        createUrun(urunRepository, "H&M Fermuarlı Kazak", "319.99", hm, erkekKazak, "Fermuarlı kazak",
                                        "Lacivert", "https://images.unsplash.com/photo-1556821840-3a63f95609a7?w=500");
                        createUrun(urunRepository, "H&M Kargo Pantolon", "379.99", hm, erkekPantolon, "Kargo pantolon",
                                        "Haki", "https://images.unsplash.com/photo-1517445312882-bc9910d016b7?w=500");
                        createUrun(urunRepository, "H&M Jogger Pantolon", "359.99", hm, erkekPantolon,
                                        "Jogger pantolon", "Siyah",
                                        "https://images.unsplash.com/photo-1552902865-b72c031ac5ea?w=500");
                        createUrun(urunRepository, "H&M Desenli Elbise", "329.99", hm, kadinElbise, "Desenli elbise",
                                        "Pembe", "https://images.unsplash.com/photo-1595777457583-95e059d581b8?w=500");
                        createUrun(urunRepository, "H&M Saten Elbise", "379.99", hm, kadinElbise, "Saten elbise",
                                        "Siyah", "https://images.unsplash.com/photo-1509631179647-0177331693ae?w=500");
                        createUrun(urunRepository, "H&M V Yaka Bluz", "169.99", hm, kadinBluz, "V yaka bluz", "Beyaz",
                                        "https://images.unsplash.com/photo-1564257631407-4deb1f99d992?w=500");
                        createUrun(urunRepository, "H&M Şifon Bluz", "189.99", hm, kadinBluz, "Şifon bluz", "Bordo",
                                        "https://images.unsplash.com/photo-1551163943-3f6a855d1153?w=500");
                        createUrun(urunRepository, "H&M Pileli Etek", "249.99", hm, kadinEtek, "Pileli etek", "Bej",
                                        "https://images.unsplash.com/photo-1591189863430-ab87e120f298?w=500");
                        createUrun(urunRepository, "H&M Denim Etek", "269.99", hm, kadinEtek, "Denim etek", "Mavi",
                                        "https://images.unsplash.com/photo-1591189863430-ab87e120f298?w=500");
                        createUrun(urunRepository, "H&M Kadın Triko Kazak", "299.99", hm, kadinKazak, "Triko kazak",
                                        "Gri", "https://images.unsplash.com/photo-1576566588028-4147f3842f27?w=500");
                        createUrun(urunRepository, "H&M Kadın Boğazlı Kazak", "319.99", hm, kadinKazak, "Boğazlı kazak",
                                        "Lacivert",
                                        "https://images.unsplash.com/photo-1434389677669-e08b4cac3105?w=500");
                        createUrun(urunRepository, "H&M Çocuk Pantolon", "159.99", hm, cocukPantolon, "Çocuk pantolon",
                                        "Gri", "https://images.unsplash.com/photo-1519238263496-d21afce7102d?w=500");
                        createUrun(urunRepository, "H&M Çocuk Jean", "179.99", hm, cocukPantolon, "Çocuk jean", "Mavi",
                                        "https://images.unsplash.com/photo-1622290291468-a28f7a7dc6a8?w=500");
                        createUrun(urunRepository, "H&M Çocuk Baskılı Tişört", "89.99", hm, cocukTisort,
                                        "Baskılı çocuk tişört",
                                        "Sarı", "https://images.unsplash.com/photo-1519238809117-22b2ff150034?w=500");
                        createUrun(urunRepository, "H&M Çocuk Fermuarlı Sweatshirt", "189.99", hm, cocukSweatshirt,
                                        "Fermuarlı sweatshirt", "Lacivert",
                                        "https://images.unsplash.com/photo-1519238263496-d21afce7102d?w=500");

                        System.out.println(">>> 210 Ürün eklendi (Her mağazadan 30 ürün)");

                        // STOKLAR
                        List<Urun> tumUrunler = urunRepository.findAll();
                        for (Urun urun : tumUrunler) {
                                for (Beden beden : tumBedenler) {
                                        urunStokRepository.save(new UrunStok(urun, beden, 20));
                                }
                        }
                        System.out.println(">>> Stoklar eklendi");

                        System.out.println("========================================");
                        System.out.println(">>> VERİ YÜKLEME TAMAMLANDI!");
                        System.out.println(">>> 7 Mağaza, 210 Ürün, 3 Kategori");
                        System.out.println("========================================");
                };
        }

        private Kullanici createMagazaSahibi(KullaniciRepository repo, PasswordEncoder encoder,
                        String kullaniciAdi, String email, String sifre, String ad, String soyad) {
                Kullanici k = new Kullanici(kullaniciAdi, email, encoder.encode(sifre), KullaniciRol.MAGAZA_SAHIBI);
                k.setAd(ad);
                k.setSoyad(soyad);
                return repo.save(k);
        }

        private Magaza createMagaza(MagazaRepository repo, String ad, Kullanici sahip, String aciklama,
                        String logoUrl) {
                Magaza m = new Magaza(ad, sahip);
                m.setAciklama(aciklama);
                m.setLogoUrl(logoUrl);
                return repo.save(m);
        }

        private void createUrun(UrunRepository repo, String ad, String fiyat, Magaza magaza,
                        AltKategori altKategori, String aciklama, String renk) {
                createUrun(repo, ad, fiyat, magaza, altKategori, aciklama, renk, null);
        }

        private void createUrun(UrunRepository repo, String ad, String fiyat, Magaza magaza,
                        AltKategori altKategori, String aciklama, String renk, String resimUrl) {
                Urun u = new Urun(ad, new BigDecimal(fiyat), magaza, altKategori);
                u.setAciklama(aciklama);
                u.setRenk(renk);
                u.setResimUrl(resimUrl);
                u.setAktif(true);
                repo.save(u);
        }
}
