package com.stoktakip.controller;

import com.stoktakip.dto.ArizaCreateRequest;
import com.stoktakip.dto.ArizaFilter;
import com.stoktakip.dto.ArizaResponse;
import com.stoktakip.dto.PageResponse;
import com.stoktakip.service.ArizaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/envanterler/{envanterGuid}/arizalar")
@Tag(name = "Arıza Yönetimi",
    description = "Envanter arızalarının, tamir süreçlerinin ve tarihsel arıza geçmişinin yönetimi")
public class EnvanterArizaController {
    private final ArizaService service;

    public EnvanterArizaController(ArizaService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createInventoryFault", summary = "Envanter İçin Arıza Kaydı Oluştur",
        description = "Belirtilen envanter için yeni arıza süreci başlatır ve ARIZA_BILDIRILDI geçmişini oluşturur.")
    public ArizaResponse create(
        @Parameter(description = "Arıza kaydı oluşturulacak envanter GUID değeri")
        @PathVariable String envanterGuid,
        @Valid @RequestBody ArizaCreateRequest request
    ) {
        return service.create(envanterGuid, request);
    }

    @GetMapping
    @Operation(operationId = "listInventoryFaultHistory", summary = "Envanter Arıza Geçmişini Getir",
        description = "Envanterin geçmiş arızalarını ve her arıza tarihindeki hesaplanmış sahiplik bilgisini sayfalı getirir.")
    public PageResponse<ArizaResponse> list(
        @PathVariable String envanterGuid,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        return service.search(new ArizaFilter(envanterGuid, null, null, null, null, null), pageable);
    }
}
