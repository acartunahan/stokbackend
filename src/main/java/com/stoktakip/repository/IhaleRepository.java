package com.stoktakip.repository;

import com.stoktakip.entity.Ihale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface IhaleRepository extends JpaRepository<Ihale, String> {
    @Override
    @EntityGraph(attributePaths = "firma")
    List<Ihale> findAll();

    @EntityGraph(attributePaths = "firma")
    @Query("select i from Ihale i where i.guid = :guid")
    Optional<Ihale> findDetayByGuid(@Param("guid") String guid);
}
