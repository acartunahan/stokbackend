package com.stoktakip.service;

import com.stoktakip.dto.EnvanterResponse;
import com.stoktakip.dto.EnvanterZimmetDurumuResponse;
import java.util.List;

public interface EnvanterService {
    List<EnvanterResponse> getAll();
    List<EnvanterResponse> getSarflar();
    List<EnvanterResponse> getDemirbaslar();
    EnvanterZimmetDurumuResponse getZimmetDurumu(String envanterGuid);
}
