package com.magazaapp.repository;

import com.magazaapp.model.Kullanici;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KullaniciRepository extends JpaRepository<Kullanici, Long> {

    Optional<Kullanici> findByKullaniciAdi(String kullaniciAdi);

    Optional<Kullanici> findByEmail(String email);

    boolean existsByKullaniciAdi(String kullaniciAdi);

    boolean existsByEmail(String email);
}
