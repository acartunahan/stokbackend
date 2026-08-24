package com.stoktakip.controller;

import com.stoktakip.dto.DepoTransferTalebi;
import com.stoktakip.dto.IadeTalebi;
import com.stoktakip.dto.ZimmetHareketYaniti;
import com.stoktakip.dto.ZimmetTalebi;
import com.stoktakip.service.ZimmetService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/zimmet")
public class ZimmetController {

    private final ZimmetService zimmetService;

    public ZimmetController(ZimmetService zimmetService) {
        this.zimmetService = zimmetService;
    }

    @PostMapping("/personel")
    @ResponseStatus(HttpStatus.CREATED)
    public ZimmetHareketYaniti personeleZimmetle(@Valid @RequestBody ZimmetTalebi talep) {
        return zimmetService.personeleZimmetle(talep);
    }

    @PostMapping("/iade")
    @ResponseStatus(HttpStatus.CREATED)
    public ZimmetHareketYaniti iadeAl(@Valid @RequestBody IadeTalebi talep) {
        return zimmetService.iadeAl(talep);
    }

    @PostMapping("/depo-transferi")
    @ResponseStatus(HttpStatus.CREATED)
    public ZimmetHareketYaniti depoyaTasi(@Valid @RequestBody DepoTransferTalebi talep) {
        return zimmetService.depoyaTasi(talep);
    }

    @GetMapping("/envanter/{guid}/personel-hareketleri")
    public List<ZimmetHareketYaniti> personelGecmisi(@PathVariable String guid) {
        return zimmetService.personelGecmisi(guid);
    }

    @GetMapping("/envanter/{guid}/depo-hareketleri")
    public List<ZimmetHareketYaniti> depoGecmisi(@PathVariable String guid) {
        return zimmetService.depoGecmisi(guid);
    }
}
