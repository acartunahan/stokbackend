package com.stoktakip.dto;

import com.stoktakip.enums.EnvanterTuru;
import java.time.LocalDateTime;

public record PersonelEnvanterResponse(
    String envanterId,
    String envanterAdi,
    EnvanterTuru envanterTuru,
    String marka,
    String model,
    LocalDateTime zimmetTarihi
) {
}
