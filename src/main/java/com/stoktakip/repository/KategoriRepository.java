package com.stoktakip.repository;

import com.stoktakip.entity.Kategori;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KategoriRepository extends JpaRepository<Kategori, String> {
    List<Kategori> findByParentIsNull();
}
