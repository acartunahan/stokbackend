package com.stoktakip.controller;

import com.stoktakip.dto.MarkaCreateRequest;
import com.stoktakip.dto.MarkaResponse;
import com.stoktakip.service.MarkaService;
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
@RequestMapping("/api/markalar")
public class MarkaController {
    private final MarkaService service;

    public MarkaController(MarkaService service) {
        this.service = service;
    }

    @GetMapping
    public List<MarkaResponse> getAll() { return service.getAll(); }

    @GetMapping("/{guid}")
    public MarkaResponse getById(@PathVariable String guid) { return service.getById(guid); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MarkaResponse create(@Valid @RequestBody MarkaCreateRequest request) {
        return service.create(request);
    }

    @DeleteMapping("/{guid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String guid) { service.delete(guid); }
}
