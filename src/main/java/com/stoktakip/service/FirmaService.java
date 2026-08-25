package com.stoktakip.service;

import com.stoktakip.dto.FirmaCreateRequest;
import com.stoktakip.dto.FirmaResponse;
import com.stoktakip.dto.FirmaUpdateRequest;
import java.util.List;

public interface FirmaService {
    List<FirmaResponse> getAll();
    FirmaResponse getById(String guid);
    FirmaResponse create(FirmaCreateRequest request);
    FirmaResponse update(String guid, FirmaUpdateRequest request);
    void delete(String guid);
}
