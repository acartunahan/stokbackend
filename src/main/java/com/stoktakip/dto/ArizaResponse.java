package com.stoktakip.dto;

import com.stoktakip.enums.ArizaDurumTipi;
import java.time.LocalDateTime;

public record ArizaResponse(
    String guid,
    String envanterGuid,
    LocalDateTime arizaTarihi,
    String arizaAciklamasi,
    String bildirimYapanPersonelKodu,
    ArizaSahiplikResponse arizaAnindakiSahiplik,
    ArizaDurumTipi sonDurum,
    boolean kapandiMi,
    LocalDateTime kapanisTarihi,
    String aciklama
) {}
