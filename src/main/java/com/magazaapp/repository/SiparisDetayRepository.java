package com.magazaapp.repository;

import com.magazaapp.model.SiparisDetay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SiparisDetayRepository extends JpaRepository<SiparisDetay, Long> {

    List<SiparisDetay> findBySiparisFisiId(Long siparisFisiId);
}
