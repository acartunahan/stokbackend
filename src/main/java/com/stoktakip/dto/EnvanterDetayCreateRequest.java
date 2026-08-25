package com.stoktakip.dto;

import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record EnvanterDetayCreateRequest(
    Long alimYolu,
    @Size(max = 20) String alimTuru,
    @Size(max = 100) String saymanlikNo,
    @Size(max = 128) String ihaleGuid,
    @Size(max = 128) String firmaGuid,
    LocalDateTime garantiBaslangicTarihi,
    LocalDateTime garantiBitisTarihi
) {
}
