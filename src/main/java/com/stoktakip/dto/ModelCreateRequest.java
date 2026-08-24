package com.stoktakip.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ModelCreateRequest(
    @NotBlank @Size(max = 255) String modelAdi,
    @Size(max = 500) String aciklama,
    @NotBlank @Size(max = 128) String markaGuid,
    String kategoriGuid
) {
}
