package com.stoktakip.repository;

import com.stoktakip.entity.Ihale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface IhaleRepository extends SoftDeleteRepository<Ihale, String> {
    @Override
    @EntityGraph(attributePaths = "firma")
    @Query("select i from Ihale i where i.silindi = false")
    List<Ihale> findAll();

    @EntityGraph(attributePaths = "firma")
    @Query("select i from Ihale i where i.guid = :guid and i.silindi = false")
    Optional<Ihale> findDetayByGuid(@Param("guid") String guid);
}
