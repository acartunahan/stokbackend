package com.stoktakip.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record SarfHareketRequest(
    @NotBlank String envanterGuid,
    @NotBlank String depoGuid,
    @NotNull @Positive Long adet,
    @Size(max = 50) String personelKodu,
    @Size(max = 1000) String aciklama
) {
}
