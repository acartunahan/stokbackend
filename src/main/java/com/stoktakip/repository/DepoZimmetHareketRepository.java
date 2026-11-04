package com.stoktakip.repository;

import com.stoktakip.entity.DepoZimmetHareket;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DepoZimmetHareketRepository extends SoftDeleteRepository<DepoZimmetHareket, Long> {

    @EntityGraph(attributePaths = {"mevcutDepo", "gidecegiDepo"})
    @Query("""
        select h from DepoZimmetHareket h
        where h.envanter.guid in :envanterGuidleri
          and h.olusturmaTarihi <= :enGecTarih
          and h.silindi = false
        order by h.envanter.guid, h.olusturmaTarihi, h.id
        """)
    List<DepoZimmetHareket> findHistoricalEvents(
        @Param("envanterGuidleri") Collection<String> envanterGuidleri,
        @Param("enGecTarih") LocalDateTime enGecTarih
    );

    @EntityGraph(attributePaths = {"envanter", "envanter.detay", "mevcutDepo", "gidecegiDepo"})
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

    @EntityGraph(attributePaths = {"envanter", "envanter.detay", "mevcutDepo", "gidecegiDepo"})
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

    @Query("""
        select h.envanter.guid, h.envanter.envanterAdi, h.gidecegiDepo.guid, h.gidecegiDepo.ad, sum(abs(h.adet)) from DepoZimmetHareket h
        where h.envanter.envanterTuru = com.stoktakip.enums.EnvanterTuru.SARF
          and upper(h.tur) in :girisTurleri
          and h.gidecegiDepo is not null
          and (:depoGuid is null or h.gidecegiDepo.guid = :depoGuid)
          and (:envanterGuid is null or h.envanter.guid = :envanterGuid)
          and h.silindi = false
          and h.envanter.silindi = false
        group by h.envanter.guid, h.envanter.envanterAdi, h.gidecegiDepo.guid, h.gidecegiDepo.ad
        """)
    List<Object[]> sumSarfGiris(
        @Param("girisTurleri") Collection<String> girisTurleri,
        @Param("depoGuid") String depoGuid,
        @Param("envanterGuid") String envanterGuid
    );

    @Query("""
        select h.envanter.guid, h.envanter.envanterAdi, h.mevcutDepo.guid, h.mevcutDepo.ad, sum(abs(h.adet)) from DepoZimmetHareket h
        where h.envanter.envanterTuru = com.stoktakip.enums.EnvanterTuru.SARF
          and upper(h.tur) in :cikisTurleri
          and h.mevcutDepo is not null
          and (:depoGuid is null or h.mevcutDepo.guid = :depoGuid)
          and (:envanterGuid is null or h.envanter.guid = :envanterGuid)
          and h.silindi = false
          and h.envanter.silindi = false
        group by h.envanter.guid, h.envanter.envanterAdi, h.mevcutDepo.guid, h.mevcutDepo.ad
        """)
    List<Object[]> sumSarfCikis(
        @Param("cikisTurleri") Collection<String> cikisTurleri,
        @Param("depoGuid") String depoGuid,
        @Param("envanterGuid") String envanterGuid
    );
}
