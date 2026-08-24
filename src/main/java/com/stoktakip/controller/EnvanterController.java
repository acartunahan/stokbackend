package com.stoktakip.controller;

import com.stoktakip.dto.EnvanterResponse;
import com.stoktakip.service.EnvanterService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/envanter")
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
}
