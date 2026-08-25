package com.stoktakip.repository;

import com.stoktakip.entity.DepoZimmetHareket;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepoZimmetHareketRepository extends JpaRepository<DepoZimmetHareket, Long> {

    List<DepoZimmetHareket> findByEnvanterGuidOrderByIdDesc(String guid);

    boolean existsByEnvanterGuid(String guid);
}
