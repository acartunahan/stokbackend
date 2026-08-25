package com.stoktakip.dto;

import java.time.LocalDateTime;

public record EnvanterDetayResponse(
    String guid,
    String envanterGuid,
    Long alimYolu,
    String alimTuru,
    String saymanlikNo,
    String ihaleGuid,
    String firmaGuid,
    LocalDateTime garantiBaslangicTarihi,
    LocalDateTime garantiBitisTarihi
) {
}
