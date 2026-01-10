package com.magazaapp.repository;

import com.magazaapp.model.AltKategori;
import com.magazaapp.model.Sezon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AltKategoriRepository extends JpaRepository<AltKategori, Long> {

    List<AltKategori> findByKategoriId(Long kategoriId);

    List<AltKategori> findBySezon(Sezon sezon);

    List<AltKategori> findByKategoriIdAndSezon(Long kategoriId, Sezon sezon);
}
