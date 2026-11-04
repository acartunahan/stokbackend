package com.stoktakip.service;

import com.stoktakip.exception.BusinessRuleException;

/** Servislerde tekrar eden GUID/metin parametresi doğrulamaları. */
public final class Guidler {
    private Guidler() {
    }

    public static String zorunlu(String value, String alanAdi) {
        if (value == null || value.isBlank()) throw new BusinessRuleException(alanAdi + " zorunludur");
        return value.trim();
    }

    public static String bosIseNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
