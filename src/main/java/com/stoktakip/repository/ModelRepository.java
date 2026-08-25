package com.stoktakip.repository;

import com.stoktakip.entity.Model;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModelRepository extends JpaRepository<Model, String> {
    @Override
    @EntityGraph(attributePaths = {"marka", "kategori"})
    List<Model> findAll();

    @EntityGraph(attributePaths = {"marka", "kategori"})
    List<Model> findByMarka_Guid(String markaGuid);

    @EntityGraph(attributePaths = {"marka", "kategori"})
    List<Model> findByMarka_GuidAndModelAdiContainingIgnoreCase(
        String markaGuid,
        String modelAdi
    );
}
