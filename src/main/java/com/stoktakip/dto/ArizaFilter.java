package com.stoktakip.dto;

import com.stoktakip.enums.ArizaDurumTipi;
import java.time.LocalDateTime;
import java.util.List;

/** Arıza listesi süzgeci. {@code durumTipi} birden çok son durum alır; boş liste süzgeç yok. */
public record ArizaFilter(
    String envanterGuid,
    List<ArizaDurumTipi> durumTipi,
    Boolean kapandiMi,
    LocalDateTime baslangicTarihi,
    LocalDateTime bitisTarihi,
    String q
) {
    public ArizaFilter {
        durumTipi = FilterValues.enums("durumTipi", durumTipi);
    }
}
