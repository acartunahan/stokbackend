package com.stoktakip.repository;

import com.stoktakip.entity.Model;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModelRepository extends JpaRepository<Model, String> {
    List<Model> findByMarka_Guid(String markaGuid);

    List<Model> findByMarka_GuidAndModelAdiContainingIgnoreCase(
        String markaGuid,
        String modelAdi
    );
}
