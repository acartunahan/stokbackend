package com.stoktakip.dto;

public record RaporOzetResponse(
    long toplamEnvanter,
    long demirbasSayisi,
    long sarfSayisi,
    long depoSayisi,
    long acikArizaSayisi,
    long garantisi30GunIcindeBitecek
) {
}
