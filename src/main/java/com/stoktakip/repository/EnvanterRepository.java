package com.stoktakip.repository;

import com.stoktakip.entity.Envanter;
import com.stoktakip.enums.EnvanterTuru;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.query.Param;

public interface EnvanterRepository extends SoftDeleteRepository<Envanter, String> {

    @EntityGraph(attributePaths = {"depo", "model", "detay", "detay.ihale", "detay.firma"})
    @Query("select e from Envanter e where e.envanterTuru = :envanterTuru and e.silindi = false")
    List<Envanter> findByEnvanterTuru(EnvanterTuru envanterTuru);

    @Override
    @EntityGraph(attributePaths = {"depo", "model", "detay", "detay.ihale", "detay.firma"})
    @Query("select e from Envanter e where e.silindi = false")
    List<Envanter> findAll();

    @EntityGraph(attributePaths = {"depo", "model", "detay", "detay.ihale", "detay.firma"})
    @Query("select e from Envanter e where e.guid = :guid and e.silindi = false")
    Optional<Envanter> findDetayByGuid(@Param("guid") String guid);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Envanter e where e.guid = :guid and e.silindi = false")
    Optional<Envanter> findByGuidForUpdate(@Param("guid") String guid);
}
