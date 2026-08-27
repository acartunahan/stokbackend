package com.stoktakip.controller;

import com.stoktakip.dto.MarkaCreateRequest;
import com.stoktakip.dto.MarkaResponse;
import com.stoktakip.dto.MarkaUpdateRequest;
import com.stoktakip.service.MarkaService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/markalar")
@Tag(name = "Marka", description = "Envanter marka tanımlarının yönetimi")
public class MarkaController {
    private final MarkaService service;

    public MarkaController(MarkaService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(operationId = "listBrands", summary = "Markaları Listele",
        description = "Aktif marka tanımlarını listeler.")
    public List<MarkaResponse> getAll() { return service.getAll(); }

    @GetMapping("/search")
    @Operation(operationId = "searchBrandsByName", summary = "Marka Adıyla Marka Bul",
        description = "Marka adında verilen metni büyük-küçük harf duyarsız biçimde arar.")
    public List<MarkaResponse> search(
        @Parameter(description = "Marka adında aranacak metin") @RequestParam String adi
    ) {
        return service.searchByName(adi);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createBrand", summary = "Marka Oluştur",
        description = "Yeni bir envanter marka tanımı oluşturur.")
    public MarkaResponse create(@Valid @RequestBody MarkaCreateRequest request) {
        return service.create(request);
    }

    @PutMapping("/{guid}")
    @Operation(operationId = "updateBrand", summary = "Marka Güncelle",
        description = "Belirtilen aktif markanın ad ve açıklama bilgilerini günceller.")
    public MarkaResponse update(
        @Parameter(description = "Güncellenecek markanın benzersiz GUID değeri") @PathVariable String guid,
        @Valid @RequestBody MarkaUpdateRequest request
    ) {
        return service.update(guid, request);
    }

    @DeleteMapping("/{guid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deleteBrand", summary = "Marka Sil",
        description = "Aktif model bağlantısı olmayan marka kaydını soft-delete yöntemiyle siler.")
    public void delete(
        @Parameter(description = "Silinecek markanın benzersiz GUID değeri") @PathVariable String guid
    ) { service.delete(guid); }
}
