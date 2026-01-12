package com.magazaapp.repository;

import com.magazaapp.model.UrunStok;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UrunStokRepository extends JpaRepository<UrunStok, Long> {

    @org.springframework.data.jpa.repository.Query("SELECT s FROM UrunStok s LEFT JOIN FETCH s.beden WHERE s.urun.id = :urunId")
    List<UrunStok> findByUrunId(@org.springframework.data.repository.query.Param("urunId") Long urunId);

    Optional<UrunStok> findByUrunIdAndBedenId(Long urunId, Long bedenId);

    void deleteByUrunId(Long urunId);
}
