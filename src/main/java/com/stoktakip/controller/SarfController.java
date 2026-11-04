package com.stoktakip.controller;

import com.stoktakip.dto.SarfHareketRequest;
import com.stoktakip.dto.SarfHareketResponse;
import com.stoktakip.dto.SarfStokResponse;
import com.stoktakip.service.SarfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sarf")
@Tag(name = "Sarf Stok", description = "Sarf malzeme giriş, çıkış ve depo bazlı stok takibi")
public class SarfController {
    private final SarfService service;

    public SarfController(SarfService service) {
        this.service = service;
    }

    @PostMapping("/giris")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "addConsumableStock", summary = "Sarf Malzeme Girişi Yap",
        description = "Belirtilen depoya verilen adet kadar sarf malzeme girişi yapar ve sarf log kaydı oluşturur.")
    public SarfHareketResponse giris(@Valid @RequestBody SarfHareketRequest request) {
        return service.giris(request);
    }

    @PostMapping("/cikis")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "consumeStock", summary = "Sarf Malzeme Çıkışı Yap",
        description = "Belirtilen depodan verilen adet kadar sarf malzeme çıkışı yapar; depodaki stok yetmezse işlemi reddeder.")
    public SarfHareketResponse cikis(@Valid @RequestBody SarfHareketRequest request) {
        return service.cikis(request);
    }

    @GetMapping("/stok")
    @Operation(operationId = "getConsumableStock", summary = "Sarf Stok Durumunu Getir",
        description = "Sarf malzemelerin depo bazında toplam giriş, çıkış ve kalan stok miktarlarını getirir.")
    public List<SarfStokResponse> getStok(
        @Parameter(description = "Sadece bu depodaki stoklar") @RequestParam(required = false) String depoGuid,
        @Parameter(description = "Sadece bu sarf malzemenin stoku") @RequestParam(required = false) String envanterGuid
    ) { return service.getStok(depoGuid, envanterGuid); }
}
