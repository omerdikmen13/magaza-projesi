package com.magazaapp.repository;

import com.magazaapp.model.Kategori;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KategoriRepository extends JpaRepository<Kategori, Long> {

    Optional<Kategori> findByAd(String ad);
}
