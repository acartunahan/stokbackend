package com.stoktakip.dto;

import com.stoktakip.enums.ArizaDurumTipi;
import com.stoktakip.enums.TamirSonucu;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ArizaDurumuCreateRequest(
    @NotNull ArizaDurumTipi durumTipi,
    LocalDateTime durumTarihi,
    @Size(max = 2000) String aciklama,
    @Size(max = 255) String servisAdi,
    @Size(max = 100) String servisReferansNo,
    @Size(max = 255) String kargoFirmasi,
    @Size(max = 100) String kargoTakipNo,
    TamirSonucu tamirSonucu,
    @Size(max = 2000) String tamirAciklamasi,
    @DecimalMin("0.00") BigDecimal masraf,
    @Size(min = 3, max = 3) String paraBirimi
) {}
