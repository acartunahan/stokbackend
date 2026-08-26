package com.stoktakip.dto;

import com.stoktakip.enums.EnvanterTuru;

public record EnvanterFilter(
    String kategoriGuid,
    String markaGuid,
    String modelGuid,
    String depoGuid,
    String firmaGuid,
    EnvanterTuru envanterTuru,
    String seriNo,
    Long demirbasNumarasi,
    String barkod,
    String q
) {
}
