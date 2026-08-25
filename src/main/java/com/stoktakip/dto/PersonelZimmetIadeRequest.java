package com.stoktakip.dto;

import jakarta.validation.constraints.Size;

public record PersonelZimmetIadeRequest(
    String depoGuid,
    @Size(max = 1000) String aciklama,
    @Size(max = 100) String islemYapanPersonel
) {
    public PersonelZimmetIadeRequest(String aciklama, String islemYapanPersonel) {
        this(null, aciklama, islemYapanPersonel);
    }
}
