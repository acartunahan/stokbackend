package com.stoktakip.controller;

import com.stoktakip.dto.PageResponse;
import com.stoktakip.dto.SarfLogResponse;
import com.stoktakip.service.SarfLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sarf-loglari")
@Tag(name = "Sarf Log", description = "Sarf malzeme işlem geçmişi")
public class SarfLogController {
    private final SarfLogService service;

    public SarfLogController(SarfLogService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(operationId = "listConsumableLogs", summary = "Sarf Loglarını Listele",
        description = "Sarf malzeme işlem kayıtlarını depo, personel, malzeme ve tarih filtreleriyle sayfalı olarak getirir.")
    public PageResponse<SarfLogResponse> search(
        @Parameter(description = "Depo GUID filtresi") @RequestParam(required = false) String depoGuid,
        @Parameter(description = "Personel kodu filtresi") @RequestParam(required = false) String personelKodu,
        @Parameter(description = "Sarf malzeme GUID filtresi") @RequestParam(required = false) String sarfMalzemeGuid,
        @Parameter(description = "İşlem tarihi için dahil başlangıç zamanı")
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime baslangicTarihi,
        @Parameter(description = "İşlem tarihi için dahil bitiş zamanı")
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime bitisTarihi,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        return service.search(depoGuid, personelKodu, sarfMalzemeGuid, baslangicTarihi, bitisTarihi, pageable);
    }
}
