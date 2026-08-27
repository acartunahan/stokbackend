package com.stoktakip.dto;

import com.stoktakip.enums.EnvanterTuru;

public record ArizaEnvanterResponse(
    String guid,
    String envanterAdi,
    EnvanterTuru envanterTuru,
    Long demirbasNumarasi,
    String seriNo,
    String barkod
) {}
