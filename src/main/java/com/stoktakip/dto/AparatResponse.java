package com.stoktakip.dto;

public record AparatResponse(
    String guid,
    String envanterGuid,
    String aparatAdi,
    String aciklama,
    Boolean aktif
) {
}
