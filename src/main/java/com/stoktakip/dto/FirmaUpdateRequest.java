package com.stoktakip.dto;

import com.stoktakip.enums.EnvanterTuru;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FirmaUpdateRequest(
    @NotBlank @Size(max = 255) String ad,
    @Size(max = 255) String yetkiliKisi,
    @Size(max = 50) String yetkiliTelefon,
    @Size(max = 255) String irtibatKisi,
    @Size(max = 255) String irtibatTelefon,
    @Size(max = 50) String telefon1,
    @Size(max = 50) String telefon2,
    @Size(max = 50) String faks,
    @Size(max = 255) String webAdresi,
    @Size(max = 500) String adres,
    @Size(max = 500) String aciklama,
    @Size(max = 32) String durum,
    @NotNull EnvanterTuru kaynakAlani
) {
}
