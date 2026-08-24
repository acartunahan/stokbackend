package com.stoktakip.service;

import com.stoktakip.dto.EnvanterResponse;
import java.util.List;

public interface EnvanterService {
    List<EnvanterResponse> getAll();
    List<EnvanterResponse> getSarflar();
    List<EnvanterResponse> getDemirbaslar();
}
