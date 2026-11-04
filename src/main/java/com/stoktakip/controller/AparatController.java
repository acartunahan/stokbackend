package com.stoktakip.controller;

import com.stoktakip.dto.AparatCreateRequest;
import com.stoktakip.dto.AparatResponse;
import com.stoktakip.dto.AparatUpdateRequest;
import com.stoktakip.service.AparatService;
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
@RequestMapping("/api/aparatlar")
@Tag(name = "Aparat", description = "Envanterlere bağlı aparat kayıtlarının yönetimi")
public class AparatController {
    private final AparatService service;

    public AparatController(AparatService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(operationId = "listAccessories", summary = "Aparatları Listele",
        description = "Aktif aparat kayıtlarını bağlı oldukları envanter bilgileriyle listeler.")
    public List<AparatResponse> getAll() { return service.getAll(); }

    @GetMapping("/{guid}")
    @Operation(operationId = "getAccessoryDetail", summary = "Aparat Detayını Getir",
        description = "Benzersiz GUID değeriyle aktif bir aparat kaydının detayını getirir.")
    public AparatResponse getById(
        @Parameter(description = "Detayı getirilecek aparatın benzersiz GUID değeri")
        @PathVariable String guid
    ) { return service.getById(guid); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createAccessory", summary = "Aparat Oluştur",
        description = "Belirtilen envantere bağlı yeni bir aparat kaydı oluşturur.")
    public AparatResponse create(@Valid @RequestBody AparatCreateRequest request) {
        return service.create(request);
    }

    @PutMapping("/{guid}")
    @Operation(operationId = "updateAccessory", summary = "Aparat Güncelle",
        description = "Belirtilen aktif aparatın adını ve açıklamasını günceller.")
    public AparatResponse update(
        @Parameter(description = "Güncellenecek aparatın benzersiz GUID değeri")
        @PathVariable String guid, @Valid @RequestBody AparatUpdateRequest request
    ) { return service.update(guid, request); }

    @DeleteMapping("/{guid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deleteAccessory", summary = "Aparat Sil",
        description = "Belirtilen aktif aparat kaydını soft-delete yöntemiyle siler.")
    public void delete(
        @Parameter(description = "Silinecek aparatın benzersiz GUID değeri")
        @PathVariable String guid
    ) { service.delete(guid); }
}
