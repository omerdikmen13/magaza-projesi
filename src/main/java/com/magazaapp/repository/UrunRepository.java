package com.magazaapp.repository;

import com.magazaapp.model.Urun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UrunRepository extends JpaRepository<Urun, Long> {

        List<Urun> findByMagazaIdAndAktifTrue(Long magazaId);

        List<Urun> findByAktifTrue();

        @Query("SELECT u FROM Urun u WHERE u.aktif = true " +
                        "AND (:magazaId IS NULL OR u.magaza.id = :magazaId) " +
                        "AND (:kategoriId IS NULL OR u.altKategori.kategori.id = :kategoriId) " +
                        "AND (:altKategoriId IS NULL OR u.altKategori.id = :altKategoriId)")
        List<Urun> findByFiltre(@Param("magazaId") Long magazaId,
                        @Param("kategoriId") Long kategoriId,
                        @Param("altKategoriId") Long altKategoriId);

        @Query("SELECT u FROM Urun u WHERE u.magaza.id = :magazaId")
        List<Urun> findByMagazaId(@Param("magazaId") Long magazaId);
}
