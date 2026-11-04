package com.stoktakip.service;

import com.stoktakip.dto.SarfHareketRequest;
import com.stoktakip.dto.SarfHareketResponse;
import com.stoktakip.dto.SarfStokResponse;
import java.util.List;

public interface SarfService {
    SarfHareketResponse giris(SarfHareketRequest request);
    SarfHareketResponse cikis(SarfHareketRequest request);
    List<SarfStokResponse> getStok(String depoGuid, String envanterGuid);
}
