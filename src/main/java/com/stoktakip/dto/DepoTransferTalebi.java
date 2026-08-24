package com.stoktakip.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepoTransferTalebi(
    @NotBlank String envanterGuid,
    @NotBlank String gidecegiDepoGuid,
    @Size(max = 1000) String aciklama,
    @Size(max = 100) String islemYapanPersonel
) {
}
