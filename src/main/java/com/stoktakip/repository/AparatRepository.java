package com.stoktakip.repository;

import com.stoktakip.entity.Aparat;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;

public interface AparatRepository extends SoftDeleteRepository<Aparat, String> {
    @Override
    @EntityGraph(attributePaths = "envanter")
    @Query("select a from Aparat a where a.silindi = false")
    List<Aparat> findAll();

    @Override
    @EntityGraph(attributePaths = "envanter")
    @Query("select a from Aparat a where a.guid = :guid and a.silindi = false")
    Optional<Aparat> findById(String guid);
}
