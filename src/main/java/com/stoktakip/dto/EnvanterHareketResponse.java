package com.stoktakip.dto;

import com.stoktakip.enums.ZimmetHareketTipi;
import java.time.LocalDateTime;

public record EnvanterHareketResponse(
    Long hareketId,
    String kaynakTuru,
    String envanterGuid,
    ZimmetHareketTipi hareketTuru,
    LocalDateTime tarih,
    String personelKodu,
    String kaynakDepoGuid,
    String kaynakDepoAdi,
    String hedefDepoGuid,
    String hedefDepoAdi,
    Long adet,
    String aciklama,
    String islemYapanPersonel,
    boolean aktif
) {
}
