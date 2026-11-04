package com.stoktakip.repository;

import com.stoktakip.entity.EnvanterDetay;
import java.util.List;
import java.time.LocalDateTime;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnvanterDetayRepository extends SoftDeleteRepository<EnvanterDetay, String> {

    @EntityGraph(attributePaths = {"envanter", "envanter.depo"})
    @Query("""
        select d from EnvanterDetay d
        where d.silindi = false
          and d.envanter.silindi = false
          and d.garantiBitisTarihi is not null
          and d.garantiBitisTarihi >= :baslangic
          and d.garantiBitisTarihi <= :bitis
        order by d.garantiBitisTarihi
        """)
    List<EnvanterDetay> findGarantisiBitecekler(
        @Param("baslangic") LocalDateTime baslangic,
        @Param("bitis") LocalDateTime bitis
    );
}
