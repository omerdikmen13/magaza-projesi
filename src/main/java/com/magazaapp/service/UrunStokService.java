package com.magazaapp.service;

import com.magazaapp.model.Beden;
import com.magazaapp.model.Urun;
import com.magazaapp.model.UrunStok;
import com.magazaapp.repository.UrunStokRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UrunStokService {

    private final UrunStokRepository urunStokRepository;

    public UrunStokService(UrunStokRepository urunStokRepository) {
        this.urunStokRepository = urunStokRepository;
    }

    /**
     * Tüm stokları getir
     */
    public List<UrunStok> getTumStoklar() {
        return urunStokRepository.findAll();
    }

    /**
     * ID'ye göre stok getir
     */
    public UrunStok getStokById(Long id) {
        return urunStokRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stok bulunamadı"));
    }

    /**
     * Ürüne göre stokları getir
     */
    public List<UrunStok> getStokByUrun(Long urunId) {
        return urunStokRepository.findByUrunId(urunId);
    }

    /**
     * Ürün ve beden ile stok getir
     */
    public UrunStok getStokByUrunAndBeden(Long urunId, Long bedenId) {
        return urunStokRepository.findByUrunIdAndBedenId(urunId, bedenId)
                .orElseThrow(() -> new RuntimeException("Stok bulunamadı"));
    }

    /**
     * Stok kaydet
     */
    @Transactional
    public UrunStok saveStok(UrunStok urunStok) {
        return urunStokRepository.save(urunStok);
    }

    /**
     * Yeni stok oluştur
     */
    @Transactional
    public UrunStok createStok(Urun urun, Beden beden, int adet) {
        UrunStok stok = new UrunStok(urun, beden, adet);
        return urunStokRepository.save(stok);
    }

    /**
     * Stok miktarını güncelle
     */
    @Transactional
    public UrunStok updateStokMiktari(Long stokId, int yeniMiktar) {
        UrunStok stok = getStokById(stokId);
        stok.setAdet(yeniMiktar);
        return urunStokRepository.save(stok);
    }

    /**
     * Stok düş (satış sonrası)
     */
    @Transactional
    public UrunStok stokDus(Long urunId, Long bedenId, int miktar) {
        UrunStok stok = getStokByUrunAndBeden(urunId, bedenId);

        if (stok.getAdet() < miktar) {
            throw new RuntimeException("Yetersiz stok! Mevcut: " + stok.getAdet() + ", İstenen: " + miktar);
        }

        stok.setAdet(stok.getAdet() - miktar);
        return urunStokRepository.save(stok);
    }

    /**
     * Stok artır (iade sonrası)
     */
    @Transactional
    public UrunStok stokArtir(Long urunId, Long bedenId, int miktar) {
        UrunStok stok = getStokByUrunAndBeden(urunId, bedenId);
        stok.setAdet(stok.getAdet() + miktar);
        return urunStokRepository.save(stok);
    }

    /**
     * Stok sil
     */
    @Transactional
    public void deleteStok(Long id) {
        urunStokRepository.deleteById(id);
    }

    /**
     * Stok kontrolü - yeterli mi?
     */
    public boolean stokYeterliMi(Long urunId, Long bedenId, int istenenMiktar) {
        try {
            UrunStok stok = getStokByUrunAndBeden(urunId, bedenId);
            return stok.getAdet() >= istenenMiktar;
        } catch (RuntimeException e) {
            return false;
        }
    }

    /**
     * Toplam stok sayısı (tüm ürünler)
     */
    public long getToplamStokAdedi() {
        return urunStokRepository.findAll().stream()
                .mapToLong(UrunStok::getAdet)
                .sum();
    }

    /**
     * Ürünün toplam stok adedi (tüm bedenler)
     */
    public long getUrunToplamStok(Long urunId) {
        return urunStokRepository.findByUrunId(urunId).stream()
                .mapToLong(UrunStok::getAdet)
                .sum();
    }

    /**
     * Stokta olmayan ürünleri getir (adet = 0)
     */
    public List<UrunStok> getTukenenStoklar() {
        return urunStokRepository.findAll().stream()
                .filter(stok -> stok.getAdet() == 0)
                .toList();
    }

    /**
     * Düşük stokta olan ürünleri getir (adet < eşik)
     */
    public List<UrunStok> getDusukStoklar(int esikDeger) {
        return urunStokRepository.findAll().stream()
                .filter(stok -> stok.getAdet() > 0 && stok.getAdet() < esikDeger)
                .toList();
    }
}
