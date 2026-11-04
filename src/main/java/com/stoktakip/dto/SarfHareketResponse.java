package com.stoktakip.dto;

import java.time.LocalDateTime;

public record SarfHareketResponse(
    Long hareketId,
    String envanterGuid,
    String envanterAdi,
    String depoGuid,
    String tur,
    Long adet,
    Long kalanStok,
    LocalDateTime tarih
) {
}
