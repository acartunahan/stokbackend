package com.stoktakip.dto;

import java.time.LocalDateTime;

public record KategoriResponse(
    String guid,
    String baslik,
    String parentId,
    String parentBaslik,
    Boolean aktif,
    LocalDateTime olusturmaTarihi,
    LocalDateTime guncellemeTarihi
) {
}
