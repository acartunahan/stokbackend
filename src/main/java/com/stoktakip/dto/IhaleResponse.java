package com.stoktakip.dto;

import java.time.LocalDateTime;

public record IhaleResponse(
    String guid,
    String firmaGuid,
    String firmaAdi,
    String ad,
    LocalDateTime ihaleTarihi,
    LocalDateTime kabulTarihi,
    LocalDateTime sozlesmeTarihi,
    Long birimKodu,
    String personel,
    String aciklama,
    String komisyonBaskani,
    String uye1,
    String uye2,
    Long yerindeGaranti,
    Long onarimSuresi,
    String durum,
    LocalDateTime olusturmaTarihi
) {
}
