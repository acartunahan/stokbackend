package com.stoktakip.controller;

import com.stoktakip.dto.EnvanterResponse;
import com.stoktakip.dto.EnvanterCreateRequest;
import com.stoktakip.dto.EnvanterUpdateRequest;
import com.stoktakip.dto.EnvanterZimmetDurumuResponse;
import com.stoktakip.dto.EnvanterFilter;
import com.stoktakip.dto.EnvanterHareketResponse;
import com.stoktakip.dto.PageResponse;
import com.stoktakip.enums.EnvanterTuru;
import com.stoktakip.service.EnvanterService;
import com.stoktakip.service.HareketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/envanter", "/api/envanterler"})
@Tag(name = "Envanter", description = "Envanter kayıtlarının sorgulanması, yönetilmesi ve hareket geçmişi")
public class EnvanterController {
    private final EnvanterService service;
    private final HareketService hareketService;

    public EnvanterController(EnvanterService service, HareketService hareketService) {
        this.service = service;
        this.hareketService = hareketService;
    }

    @GetMapping
    @Operation(operationId = "listInventories", summary = "Envanterleri Listele",
        description = "Aktif envanter kayıtlarını filtreleme, sayfalama ve sıralama seçenekleriyle listeler.")
    public PageResponse<EnvanterResponse> getAll(
        @Parameter(description = "Kategori GUID değerine göre filtre") @RequestParam(required = false) String kategoriGuid,
        @Parameter(description = "Marka GUID değerine göre filtre") @RequestParam(required = false) String markaGuid,
        @Parameter(description = "Model GUID değerine göre filtre") @RequestParam(required = false) String modelGuid,
        @Parameter(description = "Depo GUID değerine göre filtre") @RequestParam(required = false) String depoGuid,
        @Parameter(description = "Firma GUID değerine göre filtre") @RequestParam(required = false) String firmaGuid,
        @RequestParam(required = false) EnvanterTuru envanterTuru,
        @Parameter(description = "Seri numarasına göre kesin filtre") @RequestParam(required = false) String seriNo,
        @Parameter(description = "Demirbaş numarasına göre kesin filtre") @RequestParam(required = false) Long demirbasNumarasi,
        @Parameter(description = "Barkod değerine göre kesin filtre") @RequestParam(required = false) String barkod,
        @Parameter(description = "Envanter adı, seri no, barkod veya açıklamada aranacak metin") @RequestParam(required = false) String q,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        return service.search(new EnvanterFilter(kategoriGuid, markaGuid, modelGuid, depoGuid,
            firmaGuid, envanterTuru, seriNo, demirbasNumarasi, barkod, q), pageable);
    }

    @GetMapping("/lookup")
    @Operation(operationId = "lookupInventoryByIdentifier",
        summary = "Envanteri Kesin Tanımlayıcıyla Bul",
        description = "Barkod, seri numarası veya demirbaş numarasından tam olarak birini kullanarak tekil aktif envanter kaydını bulur.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Envanter başarıyla bulundu"),
        @ApiResponse(responseCode = "404", description = "Envanter bulunamadı"),
        @ApiResponse(responseCode = "422", description = "Arama kriteri eksik, birden fazla veya tekil değil")
    })
    public EnvanterResponse lookup(
        @Parameter(description = "Aranacak envanterin seri numarası") @RequestParam(required = false) String seriNo,
        @Parameter(description = "Aranacak envanterin demirbaş numarası") @RequestParam(required = false) Long demirbasNumarasi,
        @Parameter(description = "Aranacak envanterin barkod değeri") @RequestParam(required = false) String barkod
    ) {
        return service.lookup(seriNo, demirbasNumarasi, barkod);
    }

    @GetMapping("/{guid}/hareketler")
    @Operation(operationId = "getInventoryMovementHistory",
        summary = "Envanter Hareket Geçmişini Getir",
        description = "Belirtilen envanterin personel zimmetleri ve depo hareketlerinden oluşan birleşik geçmişini tarih filtreleriyle sayfalı olarak getirir.")
    public PageResponse<EnvanterHareketResponse> getHareketler(
        @Parameter(description = "Hareket geçmişi getirilecek envanterin benzersiz GUID değeri") @PathVariable String guid,
        @Parameter(description = "Hareket tarihi için dahil başlangıç zamanı")
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime baslangicTarihi,
        @Parameter(description = "Hareket tarihi için dahil bitiş zamanı")
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime bitisTarihi,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        return hareketService.getEnvanterHareketleri(
            guid, baslangicTarihi, bitisTarihi, pageable);
    }

    @GetMapping("/sarf")
    @Operation(operationId = "listConsumableInventories", summary = "Sarf Envanterlerini Listele",
        description = "Envanter türü SARF olan aktif kayıtları listeler.")
    public List<EnvanterResponse> getSarflar() {
        return service.getSarflar();
    }

    @GetMapping("/demirbas")
    @Operation(operationId = "listFixedAssetInventories", summary = "Demirbaş Envanterlerini Listele",
        description = "Envanter türü DEMIRBAS olan aktif kayıtları listeler.")
    public List<EnvanterResponse> getDemirbaslar() {
        return service.getDemirbaslar();
    }

    @GetMapping("/{guid}")
    @Operation(operationId = "getInventoryDetail", summary = "Envanter Detayını Getir",
        description = "Benzersiz GUID değeriyle aktif envanter kaydını ve satın alma detayını getirir.")
    public EnvanterResponse getById(
        @Parameter(description = "Detayı getirilecek envanterin benzersiz GUID değeri") @PathVariable String guid
    ) {
        return service.getById(guid);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createInventory", summary = "Envanter Oluştur",
        description = "Yeni envanter kaydını, varsa satın alma detayıyla birlikte oluşturur ve ilk depo giriş hareketini kaydeder.")
    public EnvanterResponse create(@Valid @RequestBody EnvanterCreateRequest request) {
        return service.create(request);
    }

    @PutMapping("/{guid}")
    @Operation(operationId = "updateInventory", summary = "Envanter Güncelle",
        description = "Belirtilen aktif envanterin tür, model ve tanımlayıcı bilgilerini günceller.")
    public EnvanterResponse update(
        @Parameter(description = "Güncellenecek envanterin benzersiz GUID değeri") @PathVariable String guid,
        @Valid @RequestBody EnvanterUpdateRequest request
    ) {
        return service.update(guid, request);
    }

    @DeleteMapping("/{guid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deleteInventory", summary = "Envanter Sil",
        description = "Hareket geçmişi bulunmayan aktif envanter kaydını soft-delete yöntemiyle siler.")
    public void delete(
        @Parameter(description = "Silinecek envanterin benzersiz GUID değeri") @PathVariable String guid
    ) {
        service.delete(guid);
    }

    @GetMapping("/{envanterGuid}/zimmet-durumu")
    @Operation(operationId = "getInventoryAssignmentStatus",
        summary = "Envanter Zimmet Durumunu Getir",
        description = "Belirtilen envanterin aktif personel zimmeti bulunup bulunmadığını ve varsa zimmet bilgilerini getirir.")
    public EnvanterZimmetDurumuResponse getZimmetDurumu(
        @Parameter(description = "Zimmet durumu sorgulanacak envanterin benzersiz GUID değeri")
        @PathVariable String envanterGuid
    ) {
        return service.getZimmetDurumu(envanterGuid);
    }
}
