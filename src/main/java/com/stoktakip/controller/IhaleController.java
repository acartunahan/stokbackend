package com.stoktakip.controller;

import com.stoktakip.dto.IhaleCreateRequest;
import com.stoktakip.dto.IhaleResponse;
import com.stoktakip.dto.IhaleUpdateRequest;
import com.stoktakip.service.IhaleService;
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
@RequestMapping("/api/ihaleler")
public class IhaleController {
    private final IhaleService service;

    public IhaleController(IhaleService service) { this.service = service; }

    @GetMapping
    public List<IhaleResponse> getAll() { return service.getAll(); }

    @GetMapping("/{guid}")
    public IhaleResponse getById(@PathVariable String guid) { return service.getById(guid); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IhaleResponse create(@Valid @RequestBody IhaleCreateRequest request) {
        return service.create(request);
    }

    @PutMapping("/{guid}")
    public IhaleResponse update(
        @PathVariable String guid, @Valid @RequestBody IhaleUpdateRequest request
    ) { return service.update(guid, request); }

    @DeleteMapping("/{guid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String guid) { service.delete(guid); }
}
