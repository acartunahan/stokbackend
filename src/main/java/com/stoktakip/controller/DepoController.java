package com.stoktakip.controller;

import com.stoktakip.dto.DepoCreateRequest;
import com.stoktakip.dto.DepoResponse;
import com.stoktakip.dto.DepoUpdateRequest;
import com.stoktakip.dto.DepoHareketResponse;
import com.stoktakip.dto.EnvanterFilter;
import com.stoktakip.dto.EnvanterResponse;
import com.stoktakip.dto.PageResponse;
import com.stoktakip.enums.EnvanterTuru;
import com.stoktakip.service.DepoService;
import com.stoktakip.service.EnvanterService;
import com.stoktakip.service.HareketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/depolar")
@Tag(name = "Depo", description = "Depo kayıtları, depo envanterleri ve depo hareketleri")
public class DepoController {
    private final DepoService service;
    private final EnvanterService envanterService;
    private final HareketService hareketService;

    public DepoController(
        DepoService service, EnvanterService envanterService, HareketService hareketService
    ) {
        this.service = service;
        this.envanterService = envanterService;
        this.hareketService = hareketService;
    }

    @GetMapping
    @Operation(operationId = "listWarehouses", summary = "Depoları Listele",
        description = "Aktif depo kayıtlarını operasyonel bilgileriyle listeler.")
    public List<DepoResponse> getAll() { return service.getAll(); }

    @GetMapping("/{guid}")
    @Operation(operationId = "getWarehouseDetail", summary = "Depo Detayını Getir",
        description = "Benzersiz GUID değeriyle aktif bir depo kaydının detayını getirir.")
    public DepoResponse getById(
        @Parameter(description = "Detayı getirilecek deponun benzersiz GUID değeri")
        @PathVariable String guid
    ) { return service.getById(guid); }

    @GetMapping("/{guid}/envanterler")
    @Operation(operationId = "listWarehouseInventories",
        summary = "Depodaki Envanterleri Listele",
        description = "Belirtilen depoda bulunan envanter kayıtlarını filtreleme, sayfalama ve sıralama seçenekleriyle listeler.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Depodaki envanterler başarıyla listelendi"),
        @ApiResponse(responseCode = "404", description = "Depo bulunamadı")
    })
    public PageResponse<EnvanterResponse> getEnvanterler(
        @Parameter(description = "Envanterleri listelenecek deponun benzersiz GUID değeri") @PathVariable String guid,
        @Parameter(description = "Kategori GUID değerine göre filtre") @RequestParam(required = false) String kategoriGuid,
        @Parameter(description = "Marka GUID değerine göre filtre") @RequestParam(required = false) String markaGuid,
        @Parameter(description = "Model GUID değerine göre filtre") @RequestParam(required = false) String modelGuid,
        @RequestParam(required = false) EnvanterTuru envanterTuru,
        @Parameter(description = "Seri numarasına göre kesin filtre") @RequestParam(required = false) String seriNo,
        @Parameter(description = "Envanter adı, seri no, barkod veya açıklamada aranacak metin") @RequestParam(required = false) String q,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        EnvanterFilter filter = new EnvanterFilter(
            kategoriGuid, markaGuid, modelGuid, guid, null, envanterTuru,
            seriNo, null, null, q);
        return envanterService.getByDepo(guid, filter, pageable);
    }

    @GetMapping("/{guid}/hareketler")
    @Operation(operationId = "getWarehouseMovementHistory",
        summary = "Depo Hareket Geçmişini Getir",
        description = "Belirtilen depoya ait giriş, çıkış ve transfer hareketlerini tarih filtreleriyle sayfalı olarak getirir.")
    public PageResponse<DepoHareketResponse> getHareketler(
        @Parameter(description = "Hareket geçmişi getirilecek deponun benzersiz GUID değeri") @PathVariable String guid,
        @Parameter(description = "Hareket tarihi için dahil başlangıç zamanı")
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime baslangicTarihi,
        @Parameter(description = "Hareket tarihi için dahil bitiş zamanı")
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime bitisTarihi,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        return hareketService.getDepoHareketleri(
            guid, baslangicTarihi, bitisTarihi, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createWarehouse", summary = "Depo Oluştur",
        description = "Yeni bir depo kaydı oluşturur.")
    public DepoResponse create(@Valid @RequestBody DepoCreateRequest request) {
        return service.create(request);
    }

    @PutMapping("/{guid}")
    @Operation(operationId = "updateWarehouse", summary = "Depo Güncelle",
        description = "Belirtilen aktif deponun tanım ve sorumlu bilgilerini günceller.")
    public DepoResponse update(
        @Parameter(description = "Güncellenecek deponun benzersiz GUID değeri")
        @PathVariable String guid, @Valid @RequestBody DepoUpdateRequest request
    ) { return service.update(guid, request); }

    @DeleteMapping("/{guid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deleteWarehouse", summary = "Depo Sil",
        description = "Belirtilen aktif depo kaydını soft-delete yöntemiyle siler.")
    public void delete(
        @Parameter(description = "Silinecek deponun benzersiz GUID değeri") @PathVariable String guid
    ) { service.delete(guid); }
}
