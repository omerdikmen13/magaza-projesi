package com.magazaapp.repository;

import com.magazaapp.model.Favori;
import com.magazaapp.model.Kullanici;
import com.magazaapp.model.Urun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriRepository extends JpaRepository<Favori, Long> {

    // Kullanıcının tüm favorilerini getir
    List<Favori> findByKullaniciOrderByEklenmeTarihiDesc(Kullanici kullanici);

    // Kullanıcının belirli bir ürünü favorilerde mi kontrol et
    Optional<Favori> findByKullaniciAndUrun(Kullanici kullanici, Urun urun);

    // Kullanıcının favori ID'lerini getir
    @Query("SELECT f.urun.id FROM Favori f WHERE f.kullanici = :kullanici")
    List<Long> findUrunIdsByKullanici(Kullanici kullanici);

    // Kullanıcının favori sayısı
    long countByKullanici(Kullanici kullanici);

    // Ürünü favorilerden sil
    @Modifying
    @Query("DELETE FROM Favori f WHERE f.kullanici = :kullanici AND f.urun = :urun")
    void deleteByKullaniciAndUrun(Kullanici kullanici, Urun urun);

    // Bir ürünün kaç kişi tarafından favorilere eklendiği
    long countByUrun(Urun urun);
}
