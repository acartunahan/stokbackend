package com.stoktakip.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record IhaleCreateRequest(
    @NotBlank @Size(max = 128) String firmaGuid,
    @NotBlank @Size(max = 100) String ad,
    LocalDateTime ihaleTarihi,
    LocalDateTime kabulTarihi,
    LocalDateTime sozlesmeTarihi,
    Long birimKodu,
    @Size(max = 100) String personel,
    @Size(max = 500) String aciklama,
    @Size(max = 100) String komisyonBaskani,
    @Size(max = 100) String uye1,
    @Size(max = 100) String uye2,
    Long yerindeGaranti,
    Long onarimSuresi,
    @Size(max = 32) String durum
) {
}
