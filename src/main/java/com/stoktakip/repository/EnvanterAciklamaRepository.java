package com.stoktakip.repository;

import com.stoktakip.entity.EnvanterAciklama;
import java.util.List;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnvanterAciklamaRepository extends SoftDeleteRepository<EnvanterAciklama, String> {

    @Query("""
        select a from EnvanterAciklama a
        where a.envanter.guid = :envanterGuid and a.silindi = false
        order by a.olusturmaTarihi desc, a.guid
        """)
    List<EnvanterAciklama> findAktifByEnvanterGuid(@Param("envanterGuid") String envanterGuid);
}
