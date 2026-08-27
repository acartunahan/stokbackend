package com.stoktakip.service;

import com.stoktakip.enums.ArizaDurumTipi;
import com.stoktakip.exception.BusinessRuleException;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class ArizaTransitionValidator {
    private static final Map<ArizaDurumTipi, Set<ArizaDurumTipi>> ALLOWED = transitions();

    private ArizaTransitionValidator() {}

    public static void validate(ArizaDurumTipi current, ArizaDurumTipi next) {
        if (next == ArizaDurumTipi.KAPATILDI) {
            throw new BusinessRuleException("KAPATILDI durumu yalnızca arıza kapatma işlemiyle eklenebilir");
        }
        if (!ALLOWED.getOrDefault(current, Set.of()).contains(next)) {
            throw new BusinessRuleException("Geçersiz arıza durum geçişi: " + current + " -> " + next);
        }
    }

    private static Map<ArizaDurumTipi, Set<ArizaDurumTipi>> transitions() {
        var map = new EnumMap<ArizaDurumTipi, Set<ArizaDurumTipi>>(ArizaDurumTipi.class);
        allow(map, ArizaDurumTipi.ARIZA_BILDIRILDI, ArizaDurumTipi.DEPOYA_SEVK_EDILDI,
            ArizaDurumTipi.SERVISE_SEVK_EDILDI, ArizaDurumTipi.ARIZA_TESPITI_YAPILIYOR,
            ArizaDurumTipi.TAMIR_BEKLIYOR, ArizaDurumTipi.HURDAYA_AYRILDI);
        allow(map, ArizaDurumTipi.DEPOYA_SEVK_EDILDI, ArizaDurumTipi.YOLDA, ArizaDurumTipi.DEPOYA_ULASTI);
        allow(map, ArizaDurumTipi.DEPOYA_ULASTI, ArizaDurumTipi.SERVISE_SEVK_EDILDI,
            ArizaDurumTipi.ARIZA_TESPITI_YAPILIYOR, ArizaDurumTipi.PERSONELE_TESLIM_EDILDI,
            ArizaDurumTipi.HURDAYA_AYRILDI);
        allow(map, ArizaDurumTipi.SERVISE_SEVK_EDILDI, ArizaDurumTipi.YOLDA, ArizaDurumTipi.SERVISE_ULASTI);
        allow(map, ArizaDurumTipi.YOLDA, ArizaDurumTipi.DEPOYA_ULASTI, ArizaDurumTipi.SERVISE_ULASTI,
            ArizaDurumTipi.DEPOYA_DONDU);
        allow(map, ArizaDurumTipi.SERVISE_ULASTI, ArizaDurumTipi.ARIZA_TESPITI_YAPILIYOR,
            ArizaDurumTipi.TAMIR_BEKLIYOR);
        allow(map, ArizaDurumTipi.ARIZA_TESPITI_YAPILIYOR, ArizaDurumTipi.TAMIR_BEKLIYOR,
            ArizaDurumTipi.TAMIRDE, ArizaDurumTipi.TAMIR_EDILEMEDI);
        allow(map, ArizaDurumTipi.TAMIR_BEKLIYOR, ArizaDurumTipi.TAMIRDE, ArizaDurumTipi.PARCA_BEKLIYOR,
            ArizaDurumTipi.TAMIR_EDILEMEDI);
        allow(map, ArizaDurumTipi.TAMIRDE, ArizaDurumTipi.PARCA_BEKLIYOR,
            ArizaDurumTipi.TAMIR_EDILDI, ArizaDurumTipi.TAMIR_EDILEMEDI);
        allow(map, ArizaDurumTipi.PARCA_BEKLIYOR, ArizaDurumTipi.TAMIRDE,
            ArizaDurumTipi.TAMIR_EDILEMEDI);
        allow(map, ArizaDurumTipi.TAMIR_EDILDI, ArizaDurumTipi.SERVISTEN_CIKTI,
            ArizaDurumTipi.DEPOYA_DONDU, ArizaDurumTipi.PERSONELE_TESLIM_EDILDI);
        allow(map, ArizaDurumTipi.TAMIR_EDILEMEDI, ArizaDurumTipi.SERVISTEN_CIKTI,
            ArizaDurumTipi.HURDAYA_AYRILDI);
        allow(map, ArizaDurumTipi.SERVISTEN_CIKTI, ArizaDurumTipi.YOLDA, ArizaDurumTipi.DEPOYA_DONDU);
        allow(map, ArizaDurumTipi.DEPOYA_DONDU, ArizaDurumTipi.PERSONELE_TESLIM_EDILDI,
            ArizaDurumTipi.HURDAYA_AYRILDI);
        return Map.copyOf(map);
    }

    private static void allow(Map<ArizaDurumTipi, Set<ArizaDurumTipi>> map,
                              ArizaDurumTipi from, ArizaDurumTipi... to) {
        map.put(from, EnumSet.of(to[0], to));
    }
}
