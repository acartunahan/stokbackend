package com.stoktakip.dto;

import com.stoktakip.enums.EnvanterTuru;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EnvanterUpdateRequest(
    @NotBlank @Size(max = 255) String envanterAdi,
    @NotNull EnvanterTuru envanterTuru,
    @Size(max = 128) String modelGuid,
    Long demirbasNumarasi,
    @Size(max = 100) String seriNo,
    @Size(max = 100) String barkod,
    Long sube,
    @Size(max = 1000) String aciklama,
    String ilSubeDaireBaskanlikNumarasi
) {
}
