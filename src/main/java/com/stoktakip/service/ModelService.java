package com.stoktakip.service;

import com.stoktakip.dto.ModelCreateRequest;
import com.stoktakip.dto.ModelResponse;
import java.util.List;

public interface ModelService {
    List<ModelResponse> getAll();
    ModelResponse getById(String guid);
    ModelResponse create(ModelCreateRequest request);
    void delete(String guid);
}
