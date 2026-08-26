package com.stoktakip.repository;

import com.stoktakip.entity.Depo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepoRepository extends SoftDeleteRepository<Depo, String> {
}
