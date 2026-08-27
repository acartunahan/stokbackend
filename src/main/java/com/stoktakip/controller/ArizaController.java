package com.stoktakip.controller;

import com.stoktakip.dto.ArizaDetailResponse;
import com.stoktakip.dto.ArizaDurumuCreateRequest;
import com.stoktakip.dto.ArizaDurumuResponse;
import com.stoktakip.dto.ArizaFilter;
import com.stoktakip.dto.ArizaKapatRequest;
import com.stoktakip.dto.ArizaResponse;
import com.stoktakip.dto.PageResponse;
import com.stoktakip.enums.ArizaDurumTipi;
import com.stoktakip.service.ArizaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/arizalar")
@Tag(name = "Arıza Yönetimi",
    description = "Envanter arızalarının, tamir süreçlerinin ve tarihsel arıza geçmişinin yönetimi")
public class ArizaController {
    private final ArizaService service;

    public ArizaController(ArizaService service) { this.service = service; }

    @GetMapping
    @Operation(operationId = "listFaults", summary = "Arıza Kayıtlarını Listele",
        description = "Arızaları envanter, son durum, açık/kapalı bilgisi, tarih ve metin ölçütleriyle sayfalı listeler.")
    public PageResponse<ArizaResponse> list(
        @RequestParam(required = false) String envanterGuid,
        @RequestParam(required = false) ArizaDurumTipi durumTipi,
        @RequestParam(required = false) Boolean kapandiMi,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime baslangicTarihi,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime bitisTarihi,
        @RequestParam(required = false) String q,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        return service.search(new ArizaFilter(envanterGuid, durumTipi, kapandiMi,
            baslangicTarihi, bitisTarihi, q), pageable);
    }

    @GetMapping("/{arizaGuid}")
    @Operation(operationId = "getFaultDetail", summary = "Arıza Detayını Getir",
        description = "Arıza, envanter özeti, arıza tarihindeki hesaplanmış sahiplik ve kronolojik süreç geçmişini getirir.")
    public ArizaDetailResponse detail(@PathVariable String arizaGuid) {
        return service.getByGuid(arizaGuid);
    }

    @PostMapping("/{arizaGuid}/durumlar")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "addFaultLifecycleStatus", summary = "Arıza Sürecine Yeni Durum Ekle",
        description = "Geçiş kurallarını doğrular; süreç olayını eklerken arızanın son durum özetini aynı transaction içinde günceller.")
    public ArizaDurumuResponse addStatus(@PathVariable String arizaGuid,
                                         @Valid @RequestBody ArizaDurumuCreateRequest request) {
        return service.addStatus(arizaGuid, request);
    }

    @GetMapping("/{arizaGuid}/durumlar")
    @Operation(operationId = "getFaultLifecycleHistory", summary = "Arıza Süreç Geçmişini Getir",
        description = "Belirtilen arızanın oluşumundan kapanışına kadar bütün süreç durumlarını kronolojik getirir.")
    public List<ArizaDurumuResponse> statuses(@PathVariable String arizaGuid) {
        return service.getStatuses(arizaGuid);
    }

    @PostMapping("/{arizaGuid}/kapat")
    @Operation(operationId = "closeFault", summary = "Arıza Kaydını Kapat",
        description = "Arızayı kapatır ve KAPATILDI süreç olayını aynı transaction içinde oluşturur.")
    public ArizaResponse close(@PathVariable String arizaGuid,
                               @Valid @RequestBody(required = false) ArizaKapatRequest request) {
        return service.close(arizaGuid, request);
    }
}
