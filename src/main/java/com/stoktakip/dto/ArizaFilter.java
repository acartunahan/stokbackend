package com.stoktakip.dto;

import com.stoktakip.enums.ArizaDurumTipi;
import java.time.LocalDateTime;

public record ArizaFilter(
    String envanterGuid,
    ArizaDurumTipi durumTipi,
    Boolean kapandiMi,
    LocalDateTime baslangicTarihi,
    LocalDateTime bitisTarihi,
    String q
) {}
