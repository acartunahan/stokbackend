package com.stoktakip.controller;

import com.stoktakip.dto.KategoriRequest;
import com.stoktakip.dto.KategoriResponse;
import com.stoktakip.dto.KategoriTreeResponse;
import com.stoktakip.service.KategoriService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kategoriler")
@Tag(name = "Kategori", description = "Hiyerarşik envanter kategori tanımlarının yönetimi")
public class KategoriController {

    private final KategoriService service;

    public KategoriController(KategoriService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(operationId = "listCategories", summary = "Kategorileri Listele",
        description = "Aktif kategori kayıtlarını üst kategori bilgileriyle listeler.")
    public ResponseEntity<List<KategoriResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{guid}")
    @Operation(operationId = "getCategoryDetail", summary = "Kategori Detayını Getir",
        description = "Benzersiz GUID değeriyle aktif kategori kaydının detayını getirir.")
    public ResponseEntity<KategoriResponse> getById(
        @Parameter(description = "Detayı getirilecek kategorinin benzersiz GUID değeri") @PathVariable String guid
    ) {
        return ResponseEntity.ok(service.getById(guid));
    }

    @GetMapping("/tree")
    @Operation(operationId = "getCategoryTree", summary = "Kategori Ağacını Getir",
        description = "Aktif kategorileri üst-alt kategori ilişkilerini koruyan hiyerarşik ağaç yapısında getirir.")
    public ResponseEntity<List<KategoriTreeResponse>> getCategoryTree() {
        return ResponseEntity.ok(service.getCategoryTree());
    }

    @PostMapping
    @Operation(operationId = "createCategory", summary = "Kategori Oluştur",
        description = "İsteğe bağlı üst kategori bağlantısıyla yeni kategori kaydı oluşturur.")
    public ResponseEntity<KategoriResponse> create(
        @Valid @RequestBody KategoriRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{guid}")
    @Operation(operationId = "updateCategory", summary = "Kategori Güncelle",
        description = "Belirtilen kategorinin başlığını veya üst kategori bağlantısını döngü oluşturmayacak şekilde günceller.")
    public ResponseEntity<KategoriResponse> update(
        @Parameter(description = "Güncellenecek kategorinin benzersiz GUID değeri") @PathVariable String guid,
        @Valid @RequestBody KategoriRequest request
    ) {
        return ResponseEntity.ok(service.update(guid, request));
    }

    @DeleteMapping("/{guid}")
    @Operation(operationId = "deleteCategory", summary = "Kategori Sil",
        description = "Aktif alt kategorisi veya bağlı modeli olmayan kategori kaydını soft-delete yöntemiyle siler.")
    public ResponseEntity<Void> delete(
        @Parameter(description = "Silinecek kategorinin benzersiz GUID değeri") @PathVariable String guid
    ) {
        service.delete(guid);
        return ResponseEntity.noContent().build();
    }
}
