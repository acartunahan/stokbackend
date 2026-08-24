package com.stoktakip.service;

import com.stoktakip.dto.MarkaCreateRequest;
import com.stoktakip.dto.MarkaResponse;
import com.stoktakip.dto.MarkaUpdateRequest;
import java.util.List;

public interface MarkaService {
    List<MarkaResponse> getAll();
    List<MarkaResponse> searchByName(String adi);
    MarkaResponse create(MarkaCreateRequest request);
    MarkaResponse update(String guid, MarkaUpdateRequest request);
    void delete(String guid);
}
