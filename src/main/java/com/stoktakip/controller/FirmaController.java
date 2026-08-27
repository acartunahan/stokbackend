package com.stoktakip.controller;

import com.stoktakip.dto.FirmaCreateRequest;
import com.stoktakip.dto.FirmaResponse;
import com.stoktakip.dto.FirmaUpdateRequest;
import com.stoktakip.service.FirmaService;
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
@RequestMapping("/api/firmalar")
@Tag(name = "Firma", description = "Tedarikçi firma kayıtlarının yönetimi")
public class FirmaController {
    private final FirmaService service;

    public FirmaController(FirmaService service) { this.service = service; }

    @GetMapping
    @Operation(operationId = "listSuppliers", summary = "Firmaları Listele",
        description = "Aktif tedarikçi firma kayıtlarını listeler.")
    public List<FirmaResponse> getAll() { return service.getAll(); }

    @GetMapping("/{guid}")
    @Operation(operationId = "getSupplierDetail", summary = "Firma Detayını Getir",
        description = "Benzersiz GUID değeriyle aktif firma kaydının iletişim ve yetkili bilgilerini getirir.")
    public FirmaResponse getById(
        @Parameter(description = "Detayı getirilecek firmanın benzersiz GUID değeri") @PathVariable String guid
    ) { return service.getById(guid); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createSupplier", summary = "Firma Oluştur",
        description = "Yeni bir tedarikçi firma kaydı oluşturur.")
    public FirmaResponse create(@Valid @RequestBody FirmaCreateRequest request) {
        return service.create(request);
    }

    @PutMapping("/{guid}")
    @Operation(operationId = "updateSupplier", summary = "Firma Güncelle",
        description = "Belirtilen aktif firmanın tanım ve iletişim bilgilerini günceller.")
    public FirmaResponse update(
        @Parameter(description = "Güncellenecek firmanın benzersiz GUID değeri")
        @PathVariable String guid, @Valid @RequestBody FirmaUpdateRequest request
    ) { return service.update(guid, request); }

    @DeleteMapping("/{guid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deleteSupplier", summary = "Firma Sil",
        description = "Belirtilen aktif firma kaydını soft-delete yöntemiyle siler.")
    public void delete(
        @Parameter(description = "Silinecek firmanın benzersiz GUID değeri") @PathVariable String guid
    ) { service.delete(guid); }
}
