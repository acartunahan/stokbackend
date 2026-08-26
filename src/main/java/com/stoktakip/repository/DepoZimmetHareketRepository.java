package com.stoktakip.repository;

import com.stoktakip.entity.DepoZimmetHareket;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DepoZimmetHareketRepository extends SoftDeleteRepository<DepoZimmetHareket, Long> {

    @Query("select h from DepoZimmetHareket h where h.envanter.guid = :guid and h.silindi = false order by h.id desc")
    List<DepoZimmetHareket> findByEnvanterGuidOrderByIdDesc(String guid);

    @Query("select count(h) > 0 from DepoZimmetHareket h where h.envanter.guid = :guid and h.silindi = false")
    boolean existsByEnvanterGuid(String guid);
}
