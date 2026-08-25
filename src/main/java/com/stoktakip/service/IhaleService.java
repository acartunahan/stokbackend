package com.stoktakip.service;

import com.stoktakip.dto.IhaleCreateRequest;
import com.stoktakip.dto.IhaleResponse;
import com.stoktakip.dto.IhaleUpdateRequest;
import java.util.List;

public interface IhaleService {
    List<IhaleResponse> getAll();
    IhaleResponse getById(String guid);
    IhaleResponse create(IhaleCreateRequest request);
    IhaleResponse update(String guid, IhaleUpdateRequest request);
    void delete(String guid);
}
