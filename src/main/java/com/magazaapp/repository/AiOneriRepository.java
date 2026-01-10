package com.magazaapp.repository;

import com.magazaapp.model.AiOneri;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiOneriRepository extends JpaRepository<AiOneri, Long> {

    List<AiOneri> findByKullaniciIdOrderByTarihDesc(Long kullaniciId);
}
