package com.stoktakip.dto;

import java.time.LocalDateTime;

public record GarantiBitenResponse(
    String envanterGuid,
    String envanterAdi,
    Long demirbasNumarasi,
    String seriNo,
    String depoGuid,
    String depoAdi,
    LocalDateTime garantiBitisTarihi,
    long kalanGun
) {
}
