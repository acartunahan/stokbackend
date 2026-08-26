package com.stoktakip.repository;

import com.stoktakip.entity.Marka;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarkaRepository extends SoftDeleteRepository<Marka, String> {
    List<Marka> findByMarkaAdiContainingIgnoreCaseAndSilindiFalse(String markaAdi);
}
