package com.stoktakip.service;

import com.stoktakip.dto.PersonelEnvanterResponse;
import com.stoktakip.dto.PersonelZimmetCreateRequest;
import com.stoktakip.dto.PersonelZimmetGecmisiResponse;
import com.stoktakip.dto.PersonelZimmetIadeRequest;
import com.stoktakip.dto.TopluIslemResponse;
import com.stoktakip.dto.TopluPersonelIadeRequest;
import com.stoktakip.dto.TopluPersonelZimmetRequest;
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

    TopluIslemResponse<PersonelZimmetGecmisiResponse> topluZimmetOlustur(
        String personelKodu, TopluPersonelZimmetRequest request
    );

    TopluIslemResponse<PersonelZimmetGecmisiResponse> topluIadeEt(
        String personelKodu, TopluPersonelIadeRequest request
    );
}
