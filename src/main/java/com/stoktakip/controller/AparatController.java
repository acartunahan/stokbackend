package com.stoktakip.controller;

import com.stoktakip.dto.AparatCreateRequest;
import com.stoktakip.dto.AparatResponse;
import com.stoktakip.service.AparatService;
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
@RequestMapping("/api/aparatlar")
public class AparatController {
    private final AparatService service;

    public AparatController(AparatService service) {
        this.service = service;
    }

    @GetMapping
    public List<AparatResponse> getAll() { return service.getAll(); }

    @GetMapping("/{guid}")
    public AparatResponse getById(@PathVariable String guid) { return service.getById(guid); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AparatResponse create(@Valid @RequestBody AparatCreateRequest request) {
        return service.create(request);
    }

    @DeleteMapping("/{guid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String guid) { service.delete(guid); }
}
