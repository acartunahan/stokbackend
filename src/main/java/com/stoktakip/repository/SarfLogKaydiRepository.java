package com.stoktakip.repository;

import com.stoktakip.entity.SarfLogKaydi;
import java.time.LocalDateTime;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SarfLogKaydiRepository extends SoftDeleteRepository<SarfLogKaydi, String> {

    @EntityGraph(attributePaths = {"depo"})
    @Query("""
        select l from SarfLogKaydi l
        where l.silindi = false
          and (:depoGuid is null or l.depo.guid = :depoGuid)
          and (:personelKodu is null or l.personelKodu = :personelKodu)
          and (:sarfMalzemeGuid is null or l.sarfMalzemeGuid = :sarfMalzemeGuid)
          and (:baslangicTarihi is null or l.olusturmaTarihi >= :baslangicTarihi)
          and (:bitisTarihi is null or l.olusturmaTarihi <= :bitisTarihi)
        """)
    Page<SarfLogKaydi> search(
        @Param("depoGuid") String depoGuid,
        @Param("personelKodu") String personelKodu,
        @Param("sarfMalzemeGuid") String sarfMalzemeGuid,
        @Param("baslangicTarihi") LocalDateTime baslangicTarihi,
        @Param("bitisTarihi") LocalDateTime bitisTarihi,
        Pageable pageable
    );
}
