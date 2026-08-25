package com.stoktakip.repository;

import com.stoktakip.entity.Aparat;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AparatRepository extends JpaRepository<Aparat, String> {
    @Override
    @EntityGraph(attributePaths = "envanter")
    List<Aparat> findAll();

    @Override
    @EntityGraph(attributePaths = "envanter")
    Optional<Aparat> findById(String guid);
}
