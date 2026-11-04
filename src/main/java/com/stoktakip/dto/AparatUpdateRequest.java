package com.stoktakip.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AparatUpdateRequest(
    @NotBlank @Size(max = 255) String aparatAdi,
    @Size(max = 1000) String aciklama
) {
}
