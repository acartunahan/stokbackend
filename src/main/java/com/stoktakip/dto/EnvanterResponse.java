package com.stoktakip.dto;

import com.stoktakip.enums.EnvanterTuru;

public record EnvanterResponse(
    String guid,
    String envanterAdi,
    EnvanterTuru envanterTuru,
    String depoGuid
) {
}
