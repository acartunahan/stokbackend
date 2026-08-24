package com.stoktakip.dto;

import jakarta.validation.constraints.Size;

public record PersonelZimmetIadeRequest(
    @Size(max = 1000) String aciklama,
    @Size(max = 100) String islemYapanPersonel
) {
}
