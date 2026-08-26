package com.stoktakip.repository;

import com.stoktakip.entity.Sayim;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SayimRepository extends SoftDeleteRepository<Sayim, String> {
}
