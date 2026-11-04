package com.stoktakip.dto;

public record SarfStokResponse(
    String envanterGuid,
    String envanterAdi,
    String depoGuid,
    String depoAdi,
    long girisAdedi,
    long cikisAdedi,
    long stok
) {
}
