package com.stoktakip.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record KategoriRequest(
    @NotBlank @Size(max = 120) String baslik,
    @Size(max = 128) String parentId
) {
}
