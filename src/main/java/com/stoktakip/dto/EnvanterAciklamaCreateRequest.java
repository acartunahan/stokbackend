package com.stoktakip.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EnvanterAciklamaCreateRequest(
    @NotBlank @Size(max = 4000) String notMetni,
    @Size(max = 500) String zimmetliPersonelMetni,
    @Size(max = 150) String ekleyen,
    @Size(max = 20) String ekleyenPersonel
) {
}
