package com.stoktakip.service;

import com.stoktakip.dto.EnvanterResponse;
import com.stoktakip.dto.EnvanterCreateRequest;
import com.stoktakip.dto.EnvanterUpdateRequest;
import com.stoktakip.dto.EnvanterZimmetDurumuResponse;
import java.util.List;

public interface EnvanterService {
    List<EnvanterResponse> getAll();
    List<EnvanterResponse> getSarflar();
    List<EnvanterResponse> getDemirbaslar();
    EnvanterResponse getById(String guid);
    EnvanterResponse create(EnvanterCreateRequest request);
    EnvanterResponse update(String guid, EnvanterUpdateRequest request);
    void delete(String guid);
    EnvanterZimmetDurumuResponse getZimmetDurumu(String envanterGuid);
}
