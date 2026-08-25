package com.stoktakip.controller;

import com.stoktakip.dto.FirmaCreateRequest;
import com.stoktakip.dto.FirmaResponse;
import com.stoktakip.dto.FirmaUpdateRequest;
import com.stoktakip.service.FirmaService;
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
@RequestMapping("/api/firmalar")
public class FirmaController {
    private final FirmaService service;

    public FirmaController(FirmaService service) { this.service = service; }

    @GetMapping
    public List<FirmaResponse> getAll() { return service.getAll(); }

    @GetMapping("/{guid}")
    public FirmaResponse getById(@PathVariable String guid) { return service.getById(guid); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FirmaResponse create(@Valid @RequestBody FirmaCreateRequest request) {
        return service.create(request);
    }

    @PutMapping("/{guid}")
    public FirmaResponse update(
        @PathVariable String guid, @Valid @RequestBody FirmaUpdateRequest request
    ) { return service.update(guid, request); }

    @DeleteMapping("/{guid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String guid) { service.delete(guid); }
}
