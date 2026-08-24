package com.stoktakip.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MarkaCreateRequest(
    @NotBlank @Size(max = 255) String markaAdi,
    @Size(max = 500) String aciklama
) {
}
