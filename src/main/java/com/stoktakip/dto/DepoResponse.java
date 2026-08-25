package com.stoktakip.dto;

import java.time.LocalDateTime;

public record DepoResponse(
    String guid,
    String ad,
    String aciklama,
    String personelKodu,
    String durum,
    LocalDateTime olusturmaTarihi
) {
}
