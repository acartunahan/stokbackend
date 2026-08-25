package com.stoktakip.dto;

import com.stoktakip.enums.EnvanterTuru;

public record EnvanterResponse(
    String guid,
    String envanterAdi,
    EnvanterTuru envanterTuru,
    String depoGuid,
    String modelGuid,
    Long demirbasNumarasi,
    String seriNo,
    String barkod,
    Long sube,
    String aciklama,
    String ilSubeDaireBaskanlikNumarasi,
    EnvanterDetayResponse detay
) {
}
