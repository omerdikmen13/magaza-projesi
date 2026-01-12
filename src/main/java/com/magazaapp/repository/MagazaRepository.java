package com.magazaapp.repository;

import com.magazaapp.model.Magaza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MagazaRepository extends JpaRepository<Magaza, Long> {

    List<Magaza> findByAktifTrue();

    List<Magaza> findBySahipId(Long sahipId);
}
