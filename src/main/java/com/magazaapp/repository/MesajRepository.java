package com.magazaapp.repository;

import com.magazaapp.model.Mesaj;
import com.magazaapp.model.Kullanici;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MesajRepository extends JpaRepository<Mesaj, Long> {

        // Belirli bir mağaza ile belirli bir müşteri arasındaki mesajlar
        @Query("SELECT m FROM Mesaj m WHERE m.magaza.id = :magazaId AND m.musteri.id = :musteriId ORDER BY m.tarih ASC")
        List<Mesaj> findByMagazaIdAndMusteriIdOrderByTarihAsc(@Param("magazaId") Long magazaId,
                        @Param("musteriId") Long musteriId);

        // Müşterinin tüm mağazalarla yaptığı sohbetlerin son mesajları
        @Query("SELECT m FROM Mesaj m WHERE m.musteri.id = :musteriId ORDER BY m.tarih DESC")
        List<Mesaj> findByMusteriIdOrderByTarihDesc(@Param("musteriId") Long musteriId);

        // Mağazaya gelen tüm mesajlar
        List<Mesaj> findByMagazaIdOrderByTarihDesc(Long magazaId);

        // Mağazanın okunmamış mesaj sayısı (müşterilerden gelen)
        @Query("SELECT COUNT(m) FROM Mesaj m WHERE m.magaza.id = :magazaId AND m.okundu = false AND m.gonderenMusteri = true")
        Long countOkunmamisByMagazaId(@Param("magazaId") Long magazaId);

        // Tüm mesajlar (admin için)
        List<Mesaj> findAllByOrderByTarihDesc();

        // Mağazaya mesaj gönderen benzersiz müşteriler
        @Query("SELECT DISTINCT m.musteri FROM Mesaj m WHERE m.magaza.id = :magazaId AND m.musteri IS NOT NULL")
        List<Kullanici> findDistinctMusterilerByMagazaId(@Param("magazaId") Long magazaId);
}
