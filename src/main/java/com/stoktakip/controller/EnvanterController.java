package com.stoktakip.controller;

import com.stoktakip.dto.EnvanterResponse;
import com.stoktakip.dto.EnvanterCreateRequest;
import com.stoktakip.dto.EnvanterUpdateRequest;
import com.stoktakip.dto.EnvanterZimmetDurumuResponse;
import com.stoktakip.service.EnvanterService;
import java.util.List;
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

    public EnvanterController(EnvanterService service) {
        this.service = service;
    }

    @GetMapping
    public List<EnvanterResponse> getAll() {
        return service.getAll();
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
