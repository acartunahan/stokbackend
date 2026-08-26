package com.stoktakip.repository;

import com.stoktakip.entity.EnvanterAciklama;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnvanterAciklamaRepository extends SoftDeleteRepository<EnvanterAciklama, String> {
}
