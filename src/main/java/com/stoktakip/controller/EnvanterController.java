package com.stoktakip.controller;

import com.stoktakip.dto.EnvanterResponse;
import com.stoktakip.dto.EnvanterCreateRequest;
import com.stoktakip.dto.EnvanterUpdateRequest;
import com.stoktakip.dto.EnvanterZimmetDurumuResponse;
import com.stoktakip.dto.EnvanterFilter;
import com.stoktakip.dto.EnvanterHareketResponse;
import com.stoktakip.dto.PageResponse;
import com.stoktakip.enums.EnvanterTuru;
import com.stoktakip.service.EnvanterService;
import com.stoktakip.service.HareketService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/envanter", "/api/envanterler"})
public class EnvanterController {
    private final EnvanterService service;
    private final HareketService hareketService;

    public EnvanterController(EnvanterService service, HareketService hareketService) {
        this.service = service;
        this.hareketService = hareketService;
    }

    @GetMapping
    public PageResponse<EnvanterResponse> getAll(
        @RequestParam(required = false) String kategoriGuid,
        @RequestParam(required = false) String markaGuid,
        @RequestParam(required = false) String modelGuid,
        @RequestParam(required = false) String depoGuid,
        @RequestParam(required = false) String firmaGuid,
        @RequestParam(required = false) EnvanterTuru envanterTuru,
        @RequestParam(required = false) String seriNo,
        @RequestParam(required = false) Long demirbasNumarasi,
        @RequestParam(required = false) String barkod,
        @RequestParam(required = false) String q,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        return service.search(new EnvanterFilter(kategoriGuid, markaGuid, modelGuid, depoGuid,
            firmaGuid, envanterTuru, seriNo, demirbasNumarasi, barkod, q), pageable);
    }

    @GetMapping("/lookup")
    public EnvanterResponse lookup(
        @RequestParam(required = false) String seriNo,
        @RequestParam(required = false) Long demirbasNumarasi,
        @RequestParam(required = false) String barkod
    ) {
        return service.lookup(seriNo, demirbasNumarasi, barkod);
    }

    @GetMapping("/{guid}/hareketler")
    public PageResponse<EnvanterHareketResponse> getHareketler(
        @PathVariable String guid,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime baslangicTarihi,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime bitisTarihi,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        return hareketService.getEnvanterHareketleri(
            guid, baslangicTarihi, bitisTarihi, pageable);
    }

    @GetMapping("/sarf")
    public List<EnvanterResponse> getSarflar() {
        return service.getSarflar();
    }

    @GetMapping("/demirbas")
    public List<EnvanterResponse> getDemirbaslar() {
        return service.getDemirbaslar();
    }

    @GetMapping("/{guid}")
    public EnvanterResponse getById(@PathVariable String guid) {
        return service.getById(guid);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EnvanterResponse create(@Valid @RequestBody EnvanterCreateRequest request) {
        return service.create(request);
    }

    @PutMapping("/{guid}")
    public EnvanterResponse update(
        @PathVariable String guid,
        @Valid @RequestBody EnvanterUpdateRequest request
    ) {
        return service.update(guid, request);
    }

    @DeleteMapping("/{guid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String guid) {
        service.delete(guid);
    }

    @GetMapping("/{envanterGuid}/zimmet-durumu")
    public EnvanterZimmetDurumuResponse getZimmetDurumu(
        @PathVariable String envanterGuid
    ) {
        return service.getZimmetDurumu(envanterGuid);
    }
}
