package com.stoktakip.service;

import com.stoktakip.dto.ModelCreateRequest;
import com.stoktakip.dto.ModelResponse;
import com.stoktakip.dto.ModelUpdateRequest;
import java.util.List;

public interface ModelService {
    List<ModelResponse> getAll();
    List<ModelResponse> getByMarkaGuid(String markaGuid);
    List<ModelResponse> searchByMarkaAndName(String markaGuid, String adi);
    ModelResponse create(ModelCreateRequest request);
    ModelResponse update(String guid, ModelUpdateRequest request);
    void delete(String guid);
}
