package com.magazaapp.repository;

import com.magazaapp.model.Beden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BedenRepository extends JpaRepository<Beden, Long> {

    Optional<Beden> findByAd(String ad);
}
