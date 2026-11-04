package com.stoktakip.service;

import com.stoktakip.dto.PageResponse;
import com.stoktakip.dto.SarfLogResponse;
import java.time.LocalDateTime;
import org.springframework.data.domain.Pageable;

public interface SarfLogService {
    PageResponse<SarfLogResponse> search(
        String depoGuid, String personelKodu, String sarfMalzemeGuid,
        LocalDateTime baslangicTarihi, LocalDateTime bitisTarihi, Pageable pageable);
}
