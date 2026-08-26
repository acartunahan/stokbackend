package com.stoktakip.repository;

import com.stoktakip.entity.Model;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ModelRepository extends SoftDeleteRepository<Model, String> {
    @Override
    @EntityGraph(attributePaths = {"marka", "kategori"})
    @Query("select m from Model m where m.silindi = false")
    List<Model> findAll();

    @EntityGraph(attributePaths = {"marka", "kategori"})
    @Query("select m from Model m where m.marka.guid = :markaGuid and m.silindi = false")
    List<Model> findByMarka_Guid(String markaGuid);

    @EntityGraph(attributePaths = {"marka", "kategori"})
    List<Model> findByMarka_GuidAndModelAdiContainingIgnoreCaseAndSilindiFalse(
        String markaGuid,
        String modelAdi
    );
}
