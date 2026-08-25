package com.stoktakip.service;

import com.stoktakip.dto.KategoriRequest;
import com.stoktakip.dto.KategoriResponse;
import com.stoktakip.dto.KategoriTreeResponse;
import java.util.List;

public interface KategoriService {
    List<KategoriResponse> getAll();
    KategoriResponse getById(String guid);
    KategoriResponse create(KategoriRequest request);
    KategoriResponse update(String guid, KategoriRequest request);
    void delete(String guid);
    List<KategoriTreeResponse> getCategoryTree();
}
