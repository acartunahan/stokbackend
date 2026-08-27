package com.stoktakip.repository;

import com.stoktakip.entity.ArizaDurumu;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArizaDurumuRepository extends SoftDeleteRepository<ArizaDurumu, String> {
    @Query("""
        select d from ArizaDurumu d
        where d.ariza.guid = :arizaGuid and d.silindi = false
        order by d.durumTarihi, d.kayitTarihi, d.guid
        """)
    List<ArizaDurumu> findHistory(@Param("arizaGuid") String arizaGuid);

    @Query("""
        select d from ArizaDurumu d
        where d.ariza.guid = :arizaGuid and d.silindi = false
        order by d.durumTarihi desc, d.kayitTarihi desc, d.guid desc limit 1
        """)
    Optional<ArizaDurumu> findLatest(@Param("arizaGuid") String arizaGuid);
}
