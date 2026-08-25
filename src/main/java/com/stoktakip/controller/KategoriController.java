package com.stoktakip.controller;

import com.stoktakip.dto.KategoriRequest;
import com.stoktakip.dto.KategoriResponse;
import com.stoktakip.dto.KategoriTreeResponse;
import com.stoktakip.service.KategoriService;
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
public class KategoriController {

    private final KategoriService service;

    public KategoriController(KategoriService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<KategoriResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{guid}")
    public ResponseEntity<KategoriResponse> getById(@PathVariable String guid) {
        return ResponseEntity.ok(service.getById(guid));
    }

    @GetMapping("/tree")
    public ResponseEntity<List<KategoriTreeResponse>> getCategoryTree() {
        return ResponseEntity.ok(service.getCategoryTree());
    }

    @PostMapping
    public ResponseEntity<KategoriResponse> create(
        @Valid @RequestBody KategoriRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{guid}")
    public ResponseEntity<KategoriResponse> update(
        @PathVariable String guid,
        @Valid @RequestBody KategoriRequest request
    ) {
        return ResponseEntity.ok(service.update(guid, request));
    }

    @DeleteMapping("/{guid}")
    public ResponseEntity<Void> delete(@PathVariable String guid) {
        service.delete(guid);
        return ResponseEntity.noContent().build();
    }
}
