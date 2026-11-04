package com.stoktakip.controller;

import com.stoktakip.dto.EnvanterAciklamaCreateRequest;
import com.stoktakip.dto.EnvanterAciklamaResponse;
import com.stoktakip.service.EnvanterAciklamaService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/envanterler/{envanterGuid}/aciklamalar")
@Tag(name = "Envanter Açıklama", description = "Envanterlere eklenen not ve açıklama kayıtlarının yönetimi")
public class EnvanterAciklamaController {
    private final EnvanterAciklamaService service;

    public EnvanterAciklamaController(EnvanterAciklamaService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(operationId = "listInventoryNotes", summary = "Envanter Açıklamalarını Listele",
        description = "Belirtilen envantere eklenmiş aktif notları en yeniden eskiye doğru listeler.")
    public List<EnvanterAciklamaResponse> getAll(
        @Parameter(description = "Notları getirilecek envanterin benzersiz GUID değeri")
        @PathVariable String envanterGuid
    ) { return service.getByEnvanter(envanterGuid); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createInventoryNote", summary = "Envanter Açıklaması Ekle",
        description = "Belirtilen envantere yeni bir not ekler.")
    public EnvanterAciklamaResponse create(
        @Parameter(description = "Not eklenecek envanterin benzersiz GUID değeri")
        @PathVariable String envanterGuid,
        @Valid @RequestBody EnvanterAciklamaCreateRequest request
    ) { return service.create(envanterGuid, request); }

    @DeleteMapping("/{aciklamaGuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deleteInventoryNote", summary = "Envanter Açıklamasını Sil",
        description = "Belirtilen envantere ait notu soft-delete yöntemiyle siler.")
    public void delete(
        @Parameter(description = "Notun bağlı olduğu envanterin benzersiz GUID değeri")
        @PathVariable String envanterGuid,
        @Parameter(description = "Silinecek notun benzersiz GUID değeri") @PathVariable String aciklamaGuid
    ) { service.delete(envanterGuid, aciklamaGuid); }
}
