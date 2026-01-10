package com.magazaapp.repository;

import com.magazaapp.model.UrunStok;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UrunStokRepository extends JpaRepository<UrunStok, Long> {

    List<UrunStok> findByUrunId(Long urunId);

    Optional<UrunStok> findByUrunIdAndBedenId(Long urunId, Long bedenId);

    void deleteByUrunId(Long urunId);
}
