package com.stoktakip.repository;

import com.stoktakip.entity.Firma;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FirmaRepository extends SoftDeleteRepository<Firma, String> {
}
