package com.stoktakip.dto;

import java.util.List;

public record ArizaDetailResponse(
    ArizaResponse ariza,
    ArizaEnvanterResponse envanter,
    List<ArizaDurumuResponse> durumGecmisi
) {}
