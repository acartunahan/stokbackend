package com.stoktakip.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TopluEnvanterItemRequest(
    @NotBlank String envanterGuid,
    @NotNull @Positive Long adet
) {
}
