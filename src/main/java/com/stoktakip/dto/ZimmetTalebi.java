package com.stoktakip.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ZimmetTalebi(
    @NotBlank String envanterGuid,
    @NotBlank @Size(max = 100) String personelKodu,
    @Size(max = 1000) String aciklama,
    @Size(max = 100) String islemYapanPersonel
) {
}
