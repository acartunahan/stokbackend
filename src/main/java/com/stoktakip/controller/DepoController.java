package com.stoktakip.controller;

import com.stoktakip.dto.DepoCreateRequest;
import com.stoktakip.dto.DepoResponse;
import com.stoktakip.dto.DepoUpdateRequest;
import com.stoktakip.service.DepoService;
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
@RequestMapping("/api/depolar")
public class DepoController {
    private final DepoService service;

    public DepoController(DepoService service) { this.service = service; }

    @GetMapping
    public List<DepoResponse> getAll() { return service.getAll(); }

    @GetMapping("/{guid}")
    public DepoResponse getById(@PathVariable String guid) { return service.getById(guid); }

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
