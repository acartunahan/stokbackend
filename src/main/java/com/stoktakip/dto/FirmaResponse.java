package com.stoktakip.dto;

import com.stoktakip.enums.EnvanterTuru;
import java.time.LocalDateTime;

public record FirmaResponse(
    String guid,
    String ad,
    String yetkiliKisi,
    String yetkiliTelefon,
    String irtibatKisi,
    String irtibatTelefon,
    String telefon1,
    String telefon2,
    String faks,
    String webAdresi,
    String adres,
    String aciklama,
    String durum,
    EnvanterTuru kaynakAlani,
    LocalDateTime olusturmaTarihi
) {
}
