package com.stoktakip.controller;

import com.stoktakip.dto.DepoCreateRequest;
import com.stoktakip.dto.DepoResponse;
import com.stoktakip.dto.DepoUpdateRequest;
import com.stoktakip.dto.DepoHareketResponse;
import com.stoktakip.dto.EnvanterFilter;
import com.stoktakip.dto.EnvanterResponse;
import com.stoktakip.dto.PageResponse;
import com.stoktakip.enums.EnvanterTuru;
import com.stoktakip.service.DepoService;
import com.stoktakip.service.EnvanterService;
import com.stoktakip.service.HareketService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
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
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/depolar")
public class DepoController {
    private final DepoService service;
    private final EnvanterService envanterService;
    private final HareketService hareketService;

    public DepoController(
        DepoService service, EnvanterService envanterService, HareketService hareketService
    ) {
        this.service = service;
        this.envanterService = envanterService;
        this.hareketService = hareketService;
    }

    @GetMapping
    public List<DepoResponse> getAll() { return service.getAll(); }

    @GetMapping("/{guid}")
    public DepoResponse getById(@PathVariable String guid) { return service.getById(guid); }

    @GetMapping("/{guid}/envanterler")
    public PageResponse<EnvanterResponse> getEnvanterler(
        @PathVariable String guid,
        @RequestParam(required = false) String kategoriGuid,
        @RequestParam(required = false) String markaGuid,
        @RequestParam(required = false) String modelGuid,
        @RequestParam(required = false) EnvanterTuru envanterTuru,
        @RequestParam(required = false) String seriNo,
        @RequestParam(required = false) String q,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        EnvanterFilter filter = new EnvanterFilter(
            kategoriGuid, markaGuid, modelGuid, guid, null, envanterTuru,
            seriNo, null, null, q);
        return envanterService.getByDepo(guid, filter, pageable);
    }

    @GetMapping("/{guid}/hareketler")
    public PageResponse<DepoHareketResponse> getHareketler(
        @PathVariable String guid,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime baslangicTarihi,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime bitisTarihi,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        return hareketService.getDepoHareketleri(
            guid, baslangicTarihi, bitisTarihi, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DepoResponse create(@Valid @RequestBody DepoCreateRequest request) {
        return service.create(request);
    }

    @PutMapping("/{guid}")
    public DepoResponse update(
        @PathVariable String guid, @Valid @RequestBody DepoUpdateRequest request
    ) { return service.update(guid, request); }

    @DeleteMapping("/{guid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String guid) { service.delete(guid); }
}
