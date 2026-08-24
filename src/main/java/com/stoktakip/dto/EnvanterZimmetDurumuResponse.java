package com.stoktakip.dto;

import java.time.LocalDateTime;

public record EnvanterZimmetDurumuResponse(
    String envanterGuid,
    String envanterAdi,
    boolean zimmetli,
    String personelKodu,
    Long hareketId,
    LocalDateTime zimmetTarihi
) {
}
