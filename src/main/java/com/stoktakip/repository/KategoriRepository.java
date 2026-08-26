package com.stoktakip.repository;

import com.stoktakip.entity.Kategori;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface KategoriRepository extends SoftDeleteRepository<Kategori, String> {
    @Query("select k from Kategori k where k.parent is null and k.silindi = false")
    List<Kategori> findByParentIsNull();
}
