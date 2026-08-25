package com.stoktakip.service;

import com.stoktakip.dto.DepoCreateRequest;
import com.stoktakip.dto.DepoResponse;
import com.stoktakip.dto.DepoUpdateRequest;
import java.util.List;

public interface DepoService {
    List<DepoResponse> getAll();
    DepoResponse getById(String guid);
    DepoResponse create(DepoCreateRequest request);
    DepoResponse update(String guid, DepoUpdateRequest request);
    void delete(String guid);
}
