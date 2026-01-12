package com.magazaapp.service;

import com.magazaapp.model.Urun;
import com.magazaapp.model.UrunStok;
import com.magazaapp.repository.UrunRepository;
import com.magazaapp.repository.UrunStokRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UrunService {

    private final UrunRepository urunRepository;
    private final UrunStokRepository urunStokRepository;

    public UrunService(UrunRepository urunRepository, UrunStokRepository urunStokRepository) {
        this.urunRepository = urunRepository;
        this.urunStokRepository = urunStokRepository;
    }

    /**
     * Ürün arama - ad, açıklama, mağaza, kategori, renk ile arama
     */
    public List<Urun> araUrun(String aramaKelimesi) {
        if (aramaKelimesi == null || aramaKelimesi.trim().length() < 2) {
            return List.of();
        }

        String arananKelime = aramaKelimesi.trim().toLowerCase();
        List<Urun> tumUrunler = urunRepository.findByAktifTrue();

        return tumUrunler.stream()
                .filter(urun -> {
                    String ad = urun.getAd() != null ? urun.getAd().toLowerCase() : "";
                    String aciklama = urun.getAciklama() != null ? urun.getAciklama().toLowerCase() : "";
                    String magazaAd = urun.getMagaza() != null && urun.getMagaza().getAd() != null
                            ? urun.getMagaza().getAd().toLowerCase()
                            : "";
                    String kategori = urun.getAltKategori() != null && urun.getAltKategori().getKategori() != null
                            ? urun.getAltKategori().getKategori().getAd().toLowerCase()
                            : "";
                    String altKategori = urun.getAltKategori() != null
                            ? urun.getAltKategori().getAd().toLowerCase()
                            : "";
                    String renk = urun.getRenk() != null ? urun.getRenk().toLowerCase() : "";

                    return ad.contains(arananKelime) ||
                            aciklama.contains(arananKelime) ||
                            magazaAd.contains(arananKelime) ||
                            kategori.contains(arananKelime) ||
                            altKategori.contains(arananKelime) ||
                            renk.contains(arananKelime);
                })
                .collect(Collectors.toList());
    }

    /**
     * Ürün detayını al
     */
    public Urun getUrunById(Long id) {
        return urunRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ürün bulunamadı"));
    }

    /**
     * Ürünün stoklarını al
     */
    /**
     * Ürünün stoklarını al
     */
    @Transactional(readOnly = true)
    public List<UrunStok> getUrunStoklari(Long urunId) {
        List<UrunStok> stoklar = urunStokRepository.findByUrunId(urunId);
        // Explicitly initialize Beden to avoid LazyInitializationException in view
        stoklar.forEach(stok -> {
            if (stok.getBeden() != null) {
                stok.getBeden().getAd();
            }
        });
        return stoklar;
    }

    /**
     * Aktif ürünleri getir
     */
    public List<Urun> getAktifUrunler() {
        return urunRepository.findByAktifTrue();
    }

    /**
     * Mağazaya göre aktif ürünleri getir
     */
    public List<Urun> getMagazaUrunleri(Long magazaId) {
        return urunRepository.findByMagazaIdAndAktifTrue(magazaId);
    }

    /**
     * Filtreli ürün arama
     */
    public List<Urun> getUrunlerByFiltre(Long magazaId, Long kategoriId, Long altKategoriId) {
        return urunRepository.findByFiltre(magazaId, kategoriId, altKategoriId);
    }

    /**
     * Ürün kaydet
     */
    @Transactional
    public Urun saveUrun(Urun urun) {
        return urunRepository.save(urun);
    }

    /**
     * Ürün sil (soft delete - aktif=false yap)
     */
    @Transactional
    public void deleteUrun(Long id) {
        Urun urun = getUrunById(id);
        urun.setAktif(false);
        urunRepository.save(urun);
    }

    /**
     * Stok kontrolü
     */
    public boolean stokKontrol(Long urunId, Long bedenId, int miktar) {
        UrunStok stok = urunStokRepository.findByUrunIdAndBedenId(urunId, bedenId)
                .orElseThrow(() -> new RuntimeException("Stok bulunamadı"));
        return stok.getAdet() >= miktar;
    }

    /**
     * Stok bilgisi al
     */
    public UrunStok getStok(Long urunId, Long bedenId) {
        return urunStokRepository.findByUrunIdAndBedenId(urunId, bedenId)
                .orElseThrow(() -> new RuntimeException("Stok bulunamadı"));
    }

    /**
     * Stok güncelle
     */
    @Transactional
    public void updateStok(Long urunId, Long bedenId, int yeniMiktar) {
        UrunStok stok = getStok(urunId, bedenId);
        stok.setAdet(yeniMiktar);
        urunStokRepository.save(stok);
    }

    /**
     * Stok düş (satış sonrası)
     */
    @Transactional
    public void stokDus(Long urunId, Long bedenId, int miktar) {
        UrunStok stok = getStok(urunId, bedenId);
        if (stok.getAdet() < miktar) {
            throw new RuntimeException("Yetersiz stok");
        }
        stok.setAdet(stok.getAdet() - miktar);
        urunStokRepository.save(stok);
    }

    /**
     * Mağazanın tüm ürünlerini getir (admin için)
     */
    public List<Urun> getAllMagazaUrunleri(Long magazaId) {
        return urunRepository.findByMagazaId(magazaId);
    }

    /**
     * Kategoriye göre ürünleri getir
     */
    public List<Urun> getUrunlerByKategori(Long kategoriId) {
        return urunRepository.findByFiltre(null, kategoriId, null);
    }

    /**
     * Mağaza ve kategoriye göre ürünleri getir
     */
    public List<Urun> getUrunlerByMagazaAndKategori(Long magazaId, Long kategoriId) {
        return urunRepository.findByFiltre(magazaId, kategoriId, null);
    }

    /**
     * Ürünleri sırala (fiyata göre)
     */
    public List<Urun> siralaUrunler(List<Urun> urunler, String siralama) {
        if ("fiyat-artan".equals(siralama)) {
            urunler.sort((a, b) -> a.getFiyat().compareTo(b.getFiyat()));
        } else if ("fiyat-azalan".equals(siralama)) {
            urunler.sort((a, b) -> b.getFiyat().compareTo(a.getFiyat()));
        }
        return urunler;
    }
}
