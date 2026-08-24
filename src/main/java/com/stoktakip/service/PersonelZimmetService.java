package com.stoktakip.service;

import com.stoktakip.dto.PersonelEnvanterResponse;
import com.stoktakip.dto.PersonelZimmetCreateRequest;
import com.stoktakip.dto.PersonelZimmetGecmisiResponse;
import com.stoktakip.dto.PersonelZimmetIadeRequest;
import java.util.List;

public interface PersonelZimmetService {

    List<PersonelEnvanterResponse> getPersonelEnvanterleri(String personelKodu);

    List<PersonelZimmetGecmisiResponse> getZimmetGecmisi(String personelKodu);

    PersonelZimmetGecmisiResponse getZimmetHareketi(String personelKodu, Long hareketId);

    PersonelZimmetGecmisiResponse zimmetOlustur(
        String personelKodu,
        PersonelZimmetCreateRequest request
    );

    PersonelZimmetGecmisiResponse iadeEt(
        String personelKodu,
        String envanterId,
        PersonelZimmetIadeRequest request
    );
}
