package com.stoktakip.repository;

import com.stoktakip.entity.EnvanterDetay;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnvanterDetayRepository extends SoftDeleteRepository<EnvanterDetay, String> {
}
