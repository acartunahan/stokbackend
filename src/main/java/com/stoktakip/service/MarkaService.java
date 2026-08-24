package com.stoktakip.service;

import com.stoktakip.dto.MarkaCreateRequest;
import com.stoktakip.dto.MarkaResponse;
import java.util.List;

public interface MarkaService {
    List<MarkaResponse> getAll();
    MarkaResponse getById(String guid);
    MarkaResponse create(MarkaCreateRequest request);
    void delete(String guid);
}
