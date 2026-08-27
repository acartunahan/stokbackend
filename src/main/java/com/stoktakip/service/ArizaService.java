package com.stoktakip.service;

import com.stoktakip.dto.ArizaCreateRequest;
import com.stoktakip.dto.ArizaDetailResponse;
import com.stoktakip.dto.ArizaDurumuCreateRequest;
import com.stoktakip.dto.ArizaDurumuResponse;
import com.stoktakip.dto.ArizaFilter;
import com.stoktakip.dto.ArizaKapatRequest;
import com.stoktakip.dto.ArizaResponse;
import com.stoktakip.dto.PageResponse;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface ArizaService {
    ArizaResponse create(String envanterGuid, ArizaCreateRequest request);
    PageResponse<ArizaResponse> search(ArizaFilter filter, Pageable pageable);
    ArizaDetailResponse getByGuid(String arizaGuid);
    ArizaDurumuResponse addStatus(String arizaGuid, ArizaDurumuCreateRequest request);
    List<ArizaDurumuResponse> getStatuses(String arizaGuid);
    ArizaResponse close(String arizaGuid, ArizaKapatRequest request);
}
