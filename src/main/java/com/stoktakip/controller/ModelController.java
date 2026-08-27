package com.stoktakip.controller;

import com.stoktakip.dto.ModelCreateRequest;
import com.stoktakip.dto.ModelResponse;
import com.stoktakip.dto.ModelUpdateRequest;
import com.stoktakip.service.ModelService;
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
@RequestMapping("/api/modeller")
@Tag(name = "Model", description = "Marka ve kategori bağlantılı envanter model tanımlarının yönetimi")
public class ModelController {
    private final ModelService service;

    public ModelController(ModelService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(operationId = "listModels", summary = "Modelleri Listele",
        description = "Aktif model tanımlarını listeler; marka GUID değeri verilirse sonuçları ilgili markayla sınırlar.")
    public List<ModelResponse> getAll(
        @Parameter(description = "Sonuçları sınırlandıracak markanın benzersiz GUID değeri")
        @RequestParam(required = false) String markaGuid
    ) {
        return markaGuid == null || markaGuid.isBlank()
            ? service.getAll()
            : service.getByMarkaGuid(markaGuid);
    }

    @GetMapping("/search")
    @Operation(operationId = "searchModelsByBrandAndName",
        summary = "Marka ve Model Adıyla Model Bul",
        description = "Belirtilen markaya ait modelleri model adındaki metne göre büyük-küçük harf duyarsız arar.")
    public List<ModelResponse> search(
        @Parameter(description = "Modelleri aranacak markanın benzersiz GUID değeri") @RequestParam String markaGuid,
        @Parameter(description = "Model adında aranacak metin") @RequestParam String adi
    ) {
        return service.searchByMarkaAndName(markaGuid, adi);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createModel", summary = "Model Oluştur",
        description = "Mevcut bir markaya ve isteğe bağlı kategoriye bağlı yeni model tanımı oluşturur.")
    public ModelResponse create(@Valid @RequestBody ModelCreateRequest request) {
        return service.create(request);
    }

    @PutMapping("/{guid}")
    @Operation(operationId = "updateModel", summary = "Model Güncelle",
        description = "Belirtilen aktif modelin ad, açıklama, marka ve kategori bağlantılarını günceller.")
    public ModelResponse update(
        @Parameter(description = "Güncellenecek modelin benzersiz GUID değeri") @PathVariable String guid,
        @Valid @RequestBody ModelUpdateRequest request
    ) {
        return service.update(guid, request);
    }

    @DeleteMapping("/{guid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deleteModel", summary = "Model Sil",
        description = "Aktif envanter bağlantısı olmayan model kaydını soft-delete yöntemiyle siler.")
    public void delete(
        @Parameter(description = "Silinecek modelin benzersiz GUID değeri") @PathVariable String guid
    ) { service.delete(guid); }
}
