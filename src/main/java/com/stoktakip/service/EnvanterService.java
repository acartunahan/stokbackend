package com.stoktakip.service;

import com.stoktakip.dto.EnvanterResponse;
import com.stoktakip.dto.EnvanterCreateRequest;
import com.stoktakip.dto.EnvanterUpdateRequest;
import com.stoktakip.dto.EnvanterZimmetDurumuResponse;
import com.stoktakip.dto.EnvanterFilter;
import com.stoktakip.dto.PageResponse;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface EnvanterService {
    List<EnvanterResponse> getAll();
    PageResponse<EnvanterResponse> search(EnvanterFilter filter, Pageable pageable);
    PageResponse<EnvanterResponse> getByDepo(
        String depoGuid, EnvanterFilter filter, Pageable pageable
    );
    EnvanterResponse lookup(String seriNo, Long demirbasNumarasi, String barkod);
    List<EnvanterResponse> getSarflar();
    List<EnvanterResponse> getDemirbaslar();
    EnvanterResponse getById(String guid);
    EnvanterResponse create(EnvanterCreateRequest request);
    EnvanterResponse update(String guid, EnvanterUpdateRequest request);
    void delete(String guid);
    EnvanterZimmetDurumuResponse getZimmetDurumu(String envanterGuid);
}
