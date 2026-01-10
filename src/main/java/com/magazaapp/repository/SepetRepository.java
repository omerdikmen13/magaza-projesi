package com.magazaapp.repository;

import com.magazaapp.model.Sepet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SepetRepository extends JpaRepository<Sepet, Long> {

    List<Sepet> findByKullaniciId(Long kullaniciId);

    Optional<Sepet> findByKullaniciIdAndUrunIdAndBedenId(Long kullaniciId, Long urunId, Long bedenId);

    void deleteByKullaniciId(Long kullaniciId);
}
