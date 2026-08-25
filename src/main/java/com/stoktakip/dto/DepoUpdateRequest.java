package com.stoktakip.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepoUpdateRequest(
    @NotBlank @Size(max = 100) String ad,
    @Size(max = 500) String aciklama,
    @Size(max = 20) String personelKodu,
    @Size(max = 32) String durum
) {
}
