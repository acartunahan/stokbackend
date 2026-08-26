package com.stoktakip.dto;

import java.util.List;

public record TopluIslemResponse<T>(
    String islemTuru,
    int toplamKayit,
    List<T> hareketler
) {
    public TopluIslemResponse {
        hareketler = List.copyOf(hareketler);
    }
}
