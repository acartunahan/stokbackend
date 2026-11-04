package com.stoktakip.dto;

import java.time.LocalDateTime;

public record EnvanterAciklamaResponse(
    String guid,
    String envanterGuid,
    String notMetni,
    String zimmetliPersonelMetni,
    String ekleyen,
    String ekleyenPersonel,
    LocalDateTime olusturmaTarihi
) {
}
