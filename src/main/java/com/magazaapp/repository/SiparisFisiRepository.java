package com.magazaapp.repository;

import com.magazaapp.model.SiparisFisi;
import com.magazaapp.model.SiparisDurum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SiparisFisiRepository extends JpaRepository<SiparisFisi, Long> {

        List<SiparisFisi> findByKullaniciId(Long kullaniciId);

        List<SiparisFisi> findByMagazaId(Long magazaId);

        List<SiparisFisi> findByMagazaIdOrderBySiparisTarihiDesc(Long magazaId);

        List<SiparisFisi> findByMagazaIdAndDurum(Long magazaId, SiparisDurum durum);

        // Günlük ciro
        @Query("SELECT COALESCE(SUM(s.toplamTutar), 0) FROM SiparisFisi s " +
                        "WHERE s.magaza.id = :magazaId AND s.durum != 'IPTAL' " +
                        "AND DATE(s.siparisTarihi) = CURRENT_DATE")
        BigDecimal getGunlukCiro(@Param("magazaId") Long magazaId);

        // Aylık ciro
        @Query("SELECT COALESCE(SUM(s.toplamTutar), 0) FROM SiparisFisi s " +
                        "WHERE s.magaza.id = :magazaId AND s.durum != 'IPTAL' " +
                        "AND YEAR(s.siparisTarihi) = YEAR(CURRENT_DATE) " +
                        "AND MONTH(s.siparisTarihi) = MONTH(CURRENT_DATE)")
        BigDecimal getAylikCiro(@Param("magazaId") Long magazaId);

        // Yıllık ciro
        @Query("SELECT COALESCE(SUM(s.toplamTutar), 0) FROM SiparisFisi s " +
                        "WHERE s.magaza.id = :magazaId AND s.durum != 'IPTAL' " +
                        "AND YEAR(s.siparisTarihi) = YEAR(CURRENT_DATE)")
        BigDecimal getYillikCiro(@Param("magazaId") Long magazaId);

        // Günlük sipariş sayısı
        @Query("SELECT COUNT(s) FROM SiparisFisi s " +
                        "WHERE s.magaza.id = :magazaId " +
                        "AND DATE(s.siparisTarihi) = CURRENT_DATE")
        Integer getGunlukSiparisSayisi(@Param("magazaId") Long magazaId);
}
