package com.stoktakip.service;

import com.stoktakip.dto.AparatCreateRequest;
import com.stoktakip.dto.AparatResponse;
import java.util.List;

public interface AparatService {
    List<AparatResponse> getAll();
    AparatResponse getById(String guid);
    AparatResponse create(AparatCreateRequest request);
    void delete(String guid);
}
