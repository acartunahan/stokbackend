package com.stoktakip.controller;

import com.stoktakip.dto.IhaleCreateRequest;
import com.stoktakip.dto.IhaleResponse;
import com.stoktakip.dto.IhaleUpdateRequest;
import com.stoktakip.service.IhaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
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

@RestController
@RequestMapping("/api/ihaleler")
@Tag(name = "İhale", description = "Firma bağlantılı ihale ve satın alma süreçlerinin yönetimi")
public class IhaleController {
    private final IhaleService service;

    public IhaleController(IhaleService service) { this.service = service; }

    @GetMapping
    @Operation(operationId = "listTenders", summary = "İhaleleri Listele",
        description = "Aktif ihale kayıtlarını bağlı firma bilgileriyle listeler.")
    public List<IhaleResponse> getAll() { return service.getAll(); }

    @GetMapping("/{guid}")
    @Operation(operationId = "getTenderDetail", summary = "İhale Detayını Getir",
        description = "Benzersiz GUID değeriyle aktif bir ihale kaydının detayını getirir.")
    public IhaleResponse getById(
        @Parameter(description = "Detayı getirilecek ihalenin benzersiz GUID değeri") @PathVariable String guid
    ) { return service.getById(guid); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createTender", summary = "İhale Oluştur",
        description = "Mevcut bir firmaya bağlı yeni ihale kaydı oluşturur.")
    public IhaleResponse create(@Valid @RequestBody IhaleCreateRequest request) {
        return service.create(request);
    }

    @PutMapping("/{guid}")
    @Operation(operationId = "updateTender", summary = "İhale Güncelle",
        description = "Belirtilen aktif ihalenin firma, tarih, komisyon ve açıklama bilgilerini günceller.")
    public IhaleResponse update(
        @Parameter(description = "Güncellenecek ihalenin benzersiz GUID değeri")
        @PathVariable String guid, @Valid @RequestBody IhaleUpdateRequest request
    ) { return service.update(guid, request); }

    @DeleteMapping("/{guid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deleteTender", summary = "İhale Sil",
        description = "Belirtilen aktif ihale kaydını soft-delete yöntemiyle siler.")
    public void delete(
        @Parameter(description = "Silinecek ihalenin benzersiz GUID değeri") @PathVariable String guid
    ) { service.delete(guid); }
}
