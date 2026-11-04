package com.stoktakip.dto;

import java.time.LocalDateTime;

public record SarfLogResponse(
    String guid,
    String personelKodu,
    String islem,
    String aciklama,
    String sarfMalzemeGuid,
    String depoGuid,
    String depoAdi,
    LocalDateTime tarih
) {
}
