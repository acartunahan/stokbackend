package com.stoktakip.dto;

import com.stoktakip.enums.EnvanterTuru;
import java.util.List;

/**
 * Envanter listesi süzgeci. Liste alanları birden çok değer alır (alan içinde VEYA,
 * alanlar arasında VE); null ya da boş liste süzgeç uygulamaz.
 */
public record EnvanterFilter(
    List<String> kategoriGuid,
    List<String> markaGuid,
    List<String> modelGuid,
    List<String> depoGuid,
    List<String> firmaGuid,
    List<EnvanterTuru> envanterTuru,
    String seriNo,
    Long demirbasNumarasi,
    String barkod,
    String q
) {
    public EnvanterFilter {
        kategoriGuid = FilterValues.texts("kategoriGuid", kategoriGuid);
        markaGuid = FilterValues.texts("markaGuid", markaGuid);
        modelGuid = FilterValues.texts("modelGuid", modelGuid);
        depoGuid = FilterValues.texts("depoGuid", depoGuid);
        firmaGuid = FilterValues.texts("firmaGuid", firmaGuid);
        envanterTuru = FilterValues.enums("envanterTuru", envanterTuru);
    }

    /** Verilen filtreyi (boş olabilir) koruyup depo koşulunu ekler. */
    public static EnvanterFilter depoIle(EnvanterFilter filter, String depoGuid) {
        if (filter == null) {
            return new EnvanterFilter(null, null, null, List.of(depoGuid), null, null, null, null, null, null);
        }
        return new EnvanterFilter(filter.kategoriGuid(), filter.markaGuid(), filter.modelGuid(), List.of(depoGuid),
            filter.firmaGuid(), filter.envanterTuru(), filter.seriNo(), filter.demirbasNumarasi(),
            filter.barkod(), filter.q());
    }
}
