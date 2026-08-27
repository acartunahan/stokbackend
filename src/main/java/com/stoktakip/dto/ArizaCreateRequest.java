package com.stoktakip.dto;

import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record ArizaCreateRequest(
    LocalDateTime arizaTarihi,
    @Size(min = 1, max = 2000) String arizaAciklamasi,
    @Size(max = 100) String bildirimYapanPersonelKodu,
    @Size(max = 2000) String aciklama
) {}
