package com.stoktakip.dto;

import com.stoktakip.enums.ZimmetHareketTipi;
import java.time.LocalDateTime;

public record ZimmetHareketYaniti(
    Long id,
    String envanterGuid,
    String envanterTuru,
    ZimmetHareketTipi hareketTipi,
    String personelKodu,
    String mevcutDepoGuid,
    String gidecegiDepoGuid,
    String aciklama,
    LocalDateTime tarih,
    boolean aktif
) {
}
