package com.stoktakip.repository;

import com.stoktakip.dto.PersonelEnvanterResponse;
import com.stoktakip.dto.PersonelZimmetGecmisiResponse;
import com.stoktakip.entity.PersonelZimmetHareket;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PersonelZimmetHareketRepository extends SoftDeleteRepository<PersonelZimmetHareket, Long> {

    @Query("select h from PersonelZimmetHareket h where h.envanter.guid = :guid and h.silindi = false order by h.id desc")
    List<PersonelZimmetHareket> findByEnvanterGuidOrderByIdDesc(String guid);

    @Query("select h from PersonelZimmetHareket h where h.envanter.guid = :guid and h.silindi = false order by h.id desc limit 1")
    Optional<PersonelZimmetHareket> findFirstByEnvanterGuidOrderByIdDesc(String guid);

    @Query("select count(h) > 0 from PersonelZimmetHareket h where h.envanter.guid = :guid and h.silindi = false")
    boolean existsByEnvanterGuid(String guid);

    @Query("""
        select new com.stoktakip.dto.PersonelEnvanterResponse(
            e.guid, e.envanterAdi, e.envanterTuru, marka.markaAdi, model.modelAdi,
            hareket.olusturmaTarihi
        )
        from PersonelZimmetHareket hareket
        join hareket.envanter e
        left join e.model model
        left join model.marka marka
        where hareket.personelKodu = :personelKodu
          and hareket.silindi = false
          and hareket.hareketTipi = com.stoktakip.enums.ZimmetHareketTipi.PERSONELE_ZIMMET
          and hareket.aktif = true
          and hareket.id = (
              select max(sonHareket.id)
              from PersonelZimmetHareket sonHareket
              where sonHareket.envanter = e
                and sonHareket.silindi = false
          )
        order by hareket.olusturmaTarihi desc, hareket.id desc
        """)
    List<PersonelEnvanterResponse> findMevcutEnvanterlerByPersonelKodu(
        @Param("personelKodu") String personelKodu
    );

    @Query("""
        select new com.stoktakip.dto.PersonelZimmetGecmisiResponse(
            hareket.id, hareket.personelKodu,
            case when e is not null then e.guid else aparatEnvanter.guid end,
            case when e is not null then e.envanterAdi else aparatEnvanter.envanterAdi end,
            case when e is not null then e.envanterTuru else aparatEnvanter.envanterTuru end,
            case when e is not null then marka.markaAdi else aparatMarka.markaAdi end,
            case when e is not null then model.modelAdi else aparatModel.modelAdi end,
            aparat.guid, aparat.aparatAdi, hareket.hareketTipi, hareket.zimmetTuru,
            hareket.aciklama, hareket.islemYapanPersonel, hareket.olusturmaTarihi,
            case when hareket.aktif = true then true else false end
        )
        from PersonelZimmetHareket hareket
        left join hareket.envanter e
        left join hareket.aparat aparat
        left join aparat.envanter aparatEnvanter
        left join e.model model
        left join model.marka marka
        left join aparatEnvanter.model aparatModel
        left join aparatModel.marka aparatMarka
        where hareket.personelKodu = :personelKodu
          and hareket.silindi = false
        order by hareket.olusturmaTarihi desc, hareket.id desc
        """)
    List<PersonelZimmetGecmisiResponse> findGecmisByPersonelKodu(
        @Param("personelKodu") String personelKodu
    );

    @Query("""
        select new com.stoktakip.dto.PersonelZimmetGecmisiResponse(
            hareket.id, hareket.personelKodu,
            case when e is not null then e.guid else aparatEnvanter.guid end,
            case when e is not null then e.envanterAdi else aparatEnvanter.envanterAdi end,
            case when e is not null then e.envanterTuru else aparatEnvanter.envanterTuru end,
            case when e is not null then marka.markaAdi else aparatMarka.markaAdi end,
            case when e is not null then model.modelAdi else aparatModel.modelAdi end,
            aparat.guid, aparat.aparatAdi, hareket.hareketTipi, hareket.zimmetTuru,
            hareket.aciklama, hareket.islemYapanPersonel, hareket.olusturmaTarihi,
            case when hareket.aktif = true then true else false end
        )
        from PersonelZimmetHareket hareket
        left join hareket.envanter e
        left join hareket.aparat aparat
        left join aparat.envanter aparatEnvanter
        left join e.model model
        left join model.marka marka
        left join aparatEnvanter.model aparatModel
        left join aparatModel.marka aparatMarka
        where hareket.personelKodu = :personelKodu
          and hareket.id = :hareketId
          and hareket.silindi = false
        """)
    Optional<PersonelZimmetGecmisiResponse> findGecmisByPersonelKoduAndId(
        @Param("personelKodu") String personelKodu,
        @Param("hareketId") Long hareketId
    );
}
