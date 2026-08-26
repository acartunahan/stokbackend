package com.stoktakip.service;

import com.stoktakip.dto.DepoHareketResponse;
import com.stoktakip.dto.EnvanterHareketResponse;
import com.stoktakip.dto.PageResponse;
import java.time.LocalDateTime;
import org.springframework.data.domain.Pageable;

public interface HareketService {
    PageResponse<EnvanterHareketResponse> getEnvanterHareketleri(
        String envanterGuid,
        LocalDateTime baslangicTarihi,
        LocalDateTime bitisTarihi,
        Pageable pageable
    );

    PageResponse<DepoHareketResponse> getDepoHareketleri(
        String depoGuid,
        LocalDateTime baslangicTarihi,
        LocalDateTime bitisTarihi,
        Pageable pageable
    );
}
