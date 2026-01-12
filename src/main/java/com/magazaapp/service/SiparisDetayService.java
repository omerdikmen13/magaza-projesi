package com.magazaapp.service;

import com.magazaapp.model.Beden;
import com.magazaapp.model.SiparisDetay;
import com.magazaapp.model.SiparisFisi;
import com.magazaapp.model.Urun;
import com.magazaapp.repository.SiparisDetayRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SiparisDetayService {

    private final SiparisDetayRepository siparisDetayRepository;

    public SiparisDetayService(SiparisDetayRepository siparisDetayRepository) {
        this.siparisDetayRepository = siparisDetayRepository;
    }

    /**
     * Tüm sipariş detaylarını getir
     */
    public List<SiparisDetay> getTumSiparisDetaylari() {
        return siparisDetayRepository.findAll();
    }

    /**
     * ID'ye göre sipariş detayı getir
     */
    public SiparisDetay getSiparisDetayById(Long id) {
        return siparisDetayRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sipariş detayı bulunamadı"));
    }

    /**
     * Sipariş fişine göre detayları getir
     */
    public List<SiparisDetay> getDetaylarBySiparisFisi(Long siparisFisiId) {
        return siparisDetayRepository.findBySiparisFisiId(siparisFisiId);
    }

    /**
     * Sipariş detayı kaydet
     */
    @Transactional
    public SiparisDetay saveSiparisDetay(SiparisDetay siparisDetay) {
        return siparisDetayRepository.save(siparisDetay);
    }

    /**
     * Yeni sipariş detayı oluştur
     */
    @Transactional
    public SiparisDetay createSiparisDetay(SiparisFisi siparisFisi, Urun urun,
            Beden beden, int adet,
            BigDecimal birimFiyat) {
        SiparisDetay detay = new SiparisDetay();
        detay.setSiparisFisi(siparisFisi);
        detay.setUrun(urun);
        detay.setBeden(beden);
        detay.setAdet(adet);
        detay.setBirimFiyat(birimFiyat);
        detay.setToplamFiyat(birimFiyat.multiply(BigDecimal.valueOf(adet)));

        return siparisDetayRepository.save(detay);
    }

    /**
     * Sipariş detayı güncelle
     */
    @Transactional
    public SiparisDetay updateSiparisDetay(Long id, int yeniAdet) {
        SiparisDetay detay = getSiparisDetayById(id);
        detay.setAdet(yeniAdet);
        detay.setToplamFiyat(detay.getBirimFiyat().multiply(BigDecimal.valueOf(yeniAdet)));
        return siparisDetayRepository.save(detay);
    }

    /**
     * Sipariş detayı sil
     */
    @Transactional
    public void deleteSiparisDetay(Long id) {
        siparisDetayRepository.deleteById(id);
    }

    /**
     * Sipariş fişinin toplam tutarını hesapla
     */
    public BigDecimal getSiparisFisiToplamTutar(Long siparisFisiId) {
        List<SiparisDetay> detaylar = getDetaylarBySiparisFisi(siparisFisiId);
        return detaylar.stream()
                .map(SiparisDetay::getToplamFiyat)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Sipariş fişindeki toplam ürün adedi
     */
    public int getSiparisFisiToplamAdet(Long siparisFisiId) {
        List<SiparisDetay> detaylar = getDetaylarBySiparisFisi(siparisFisiId);
        return detaylar.stream()
                .mapToInt(SiparisDetay::getAdet)
                .sum();
    }

    /**
     * Sipariş fişindeki ürün çeşidi sayısı
     */
    public long getSiparisFisiUrunCesidiSayisi(Long siparisFisiId) {
        return getDetaylarBySiparisFisi(siparisFisiId).size();
    }

    /**
     * Ürünün toplam satış adedini getir (tüm siparişlerde)
     */
    public long getUrunToplamSatisAdedi(Long urunId) {
        return siparisDetayRepository.findAll().stream()
                .filter(detay -> detay.getUrun().getId().equals(urunId))
                .mapToLong(SiparisDetay::getAdet)
                .sum();
    }
}
