package com.stoktakip.dto;

public record ModelResponse(
    String guid,
    String modelAdi,
    String aciklama,
    String markaGuid,
    String markaAdi,
    String kategoriGuid,
    Boolean aktif
) {
}
