package com.stoktakip.repository;

import com.stoktakip.entity.DepoZimmetHareket;
import java.util.List;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DepoZimmetHareketRepository extends SoftDeleteRepository<DepoZimmetHareket, Long> {

    @EntityGraph(attributePaths = {"envanter", "mevcutDepo", "gidecegiDepo"})
    @Query("""
        select h from DepoZimmetHareket h
        where h.envanter.guid = :guid
          and h.silindi = false
          and h.envanter.silindi = false
          and (:baslangicTarihi is null or h.olusturmaTarihi >= :baslangicTarihi)
          and (:bitisTarihi is null or h.olusturmaTarihi <= :bitisTarihi)
        """)
    Page<DepoZimmetHareket> findTimelineByEnvanterGuid(
        @Param("guid") String guid,
        @Param("baslangicTarihi") LocalDateTime baslangicTarihi,
        @Param("bitisTarihi") LocalDateTime bitisTarihi,
        Pageable pageable
    );

    @EntityGraph(attributePaths = {"envanter", "mevcutDepo", "gidecegiDepo"})
    @Query("""
        select h from DepoZimmetHareket h
        where (h.mevcutDepo.guid = :depoGuid or h.gidecegiDepo.guid = :depoGuid)
          and h.silindi = false
          and h.envanter.silindi = false
          and (:baslangicTarihi is null or h.olusturmaTarihi >= :baslangicTarihi)
          and (:bitisTarihi is null or h.olusturmaTarihi <= :bitisTarihi)
        """)
    Page<DepoZimmetHareket> findByDepoGuid(
        @Param("depoGuid") String depoGuid,
        @Param("baslangicTarihi") LocalDateTime baslangicTarihi,
        @Param("bitisTarihi") LocalDateTime bitisTarihi,
        Pageable pageable
    );

    @Query("select h from DepoZimmetHareket h where h.envanter.guid = :guid and h.silindi = false order by h.id desc")
    List<DepoZimmetHareket> findByEnvanterGuidOrderByIdDesc(String guid);

    @Query("select count(h) > 0 from DepoZimmetHareket h where h.envanter.guid = :guid and h.silindi = false")
    boolean existsByEnvanterGuid(String guid);
}
