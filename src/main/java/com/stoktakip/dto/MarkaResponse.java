package com.stoktakip.dto;

import java.time.LocalDateTime;

public record MarkaResponse(
    String guid,
    String markaAdi,
    String aciklama,
    Boolean aktif,
    LocalDateTime olusturmaTarihi,
    LocalDateTime guncellemeTarihi
) {
}
