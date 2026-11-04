package com.stoktakip.service;

import com.stoktakip.dto.EnvanterAciklamaCreateRequest;
import com.stoktakip.dto.EnvanterAciklamaResponse;
import java.util.List;

public interface EnvanterAciklamaService {
    List<EnvanterAciklamaResponse> getByEnvanter(String envanterGuid);
    EnvanterAciklamaResponse create(String envanterGuid, EnvanterAciklamaCreateRequest request);
    void delete(String envanterGuid, String aciklamaGuid);
}
