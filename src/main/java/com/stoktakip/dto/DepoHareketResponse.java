package com.stoktakip.dto;

import com.stoktakip.enums.ZimmetHareketTipi;
import java.time.LocalDateTime;

public record DepoHareketResponse(
    Long hareketId,
    String envanterGuid,
    String envanterAdi,
    ZimmetHareketTipi hareketTuru,
    String hareketYonu,
    LocalDateTime tarih,
    String kaynakDepoGuid,
    String kaynakDepoAdi,
    String hedefDepoGuid,
    String hedefDepoAdi,
    Long adet,
    String aciklama,
    String islemYapanPersonel
) {
}
