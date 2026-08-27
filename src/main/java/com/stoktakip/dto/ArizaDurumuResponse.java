package com.stoktakip.dto;

import com.stoktakip.enums.ArizaDurumTipi;
import com.stoktakip.enums.TamirSonucu;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ArizaDurumuResponse(
    String guid,
    String arizaGuid,
    ArizaDurumTipi durumTipi,
    LocalDateTime durumTarihi,
    String aciklama,
    String servisAdi,
    String servisReferansNo,
    String kargoFirmasi,
    String kargoTakipNo,
    TamirSonucu tamirSonucu,
    String tamirAciklamasi,
    BigDecimal masraf,
    String paraBirimi
) {}
