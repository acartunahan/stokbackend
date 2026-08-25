package com.stoktakip.dto;

import java.util.List;

public record KategoriTreeResponse(
    String guid,
    String baslik,
    List<KategoriTreeResponse> altKategoriler
) {
}
