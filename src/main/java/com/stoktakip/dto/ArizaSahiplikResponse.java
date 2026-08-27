package com.stoktakip.dto;

import com.stoktakip.enums.EnvanterSahiplikTipi;

public record ArizaSahiplikResponse(
    EnvanterSahiplikTipi tip,
    String personelKodu,
    String personelAdi,
    String depoGuid,
    String depoAdi
) {
    public static ArizaSahiplikResponse belirsiz() {
        return new ArizaSahiplikResponse(EnvanterSahiplikTipi.BELIRSIZ, null, null, null, null);
    }
}
