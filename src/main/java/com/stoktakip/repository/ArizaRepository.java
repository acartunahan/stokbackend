package com.stoktakip.repository;

import com.stoktakip.entity.Ariza;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArizaRepository extends SoftDeleteRepository<Ariza, String>,
    JpaSpecificationExecutor<Ariza> {

    @EntityGraph(attributePaths = "envanter")
    @Query("select a from Ariza a where a.guid = :guid and a.silindi = false")
    Optional<Ariza> findDetailByGuid(@Param("guid") String guid);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = "envanter")
    @Query("select a from Ariza a where a.guid = :guid and a.silindi = false")
    Optional<Ariza> findByGuidForUpdate(@Param("guid") String guid);

    @Query("""
        select count(a) > 0 from Ariza a
        where a.envanter.guid = :envanterGuid and a.kapandiMi = false and a.silindi = false
        """)
    boolean existsOpenByEnvanterGuid(@Param("envanterGuid") String envanterGuid);

    @Query("select count(a) from Ariza a where a.kapandiMi = false and a.silindi = false")
    long countAcik();
}
