package com.stoktakip.dto;

import com.stoktakip.enums.EnvanterTuru;

public record EnvanterFilter(
    String kategoriGuid,
    String markaGuid,
    String modelGuid,
    String depoGuid,
    String firmaGuid,
    EnvanterTuru envanterTuru,
    String seriNo,
    Long demirbasNumarasi,
    String barkod,
    String q
) {
    /** Verilen filtreyi (boş olabilir) koruyup depo koşulunu ekler. */
    public static EnvanterFilter depoIle(EnvanterFilter filter, String depoGuid) {
        if (filter == null) {
            return new EnvanterFilter(null, null, null, depoGuid, null, null, null, null, null, null);
        }
        return new EnvanterFilter(filter.kategoriGuid(), filter.markaGuid(), filter.modelGuid(), depoGuid,
            filter.firmaGuid(), filter.envanterTuru(), filter.seriNo(), filter.demirbasNumarasi(),
            filter.barkod(), filter.q());
    }
}
