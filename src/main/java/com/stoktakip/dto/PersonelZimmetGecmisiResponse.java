package com.stoktakip.dto;

import com.stoktakip.enums.EnvanterTuru;
import com.stoktakip.enums.ZimmetHareketTipi;
import java.time.LocalDateTime;

public record PersonelZimmetGecmisiResponse(
    Long hareketId,
    String personelKodu,
    String envanterId,
    String envanterAdi,
    EnvanterTuru envanterTuru,
    String marka,
    String model,
    String aparatId,
    String aparatAdi,
    ZimmetHareketTipi hareketTipi,
    String zimmetTuru,
    String aciklama,
    String islemYapanPersonel,
    LocalDateTime tarih,
    boolean aktif
) {
}
