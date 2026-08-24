package com.stoktakip.controller;

import com.stoktakip.dto.PersonelEnvanterResponse;
import com.stoktakip.dto.PersonelZimmetCreateRequest;
import com.stoktakip.dto.PersonelZimmetGecmisiResponse;
import com.stoktakip.dto.PersonelZimmetIadeRequest;
import com.stoktakip.service.PersonelZimmetService;
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
@RequestMapping("/api/personeller/{personelKodu}")
public class PersonelZimmetController {

    private final PersonelZimmetService personelZimmetService;

    public PersonelZimmetController(PersonelZimmetService personelZimmetService) {
        this.personelZimmetService = personelZimmetService;
    }

    @GetMapping("/envanterler")
    public List<PersonelEnvanterResponse> getPersonelEnvanterleri(@PathVariable String personelKodu) {
        return personelZimmetService.getPersonelEnvanterleri(personelKodu);
    }

    @GetMapping("/zimmet-gecmisi")
    public List<PersonelZimmetGecmisiResponse> getZimmetGecmisi(@PathVariable String personelKodu) {
        return personelZimmetService.getZimmetGecmisi(personelKodu);
    }

    @GetMapping("/zimmet-gecmisi/{hareketId}")
    public PersonelZimmetGecmisiResponse getZimmetHareketi(
        @PathVariable String personelKodu,
        @PathVariable Long hareketId
    ) {
        return personelZimmetService.getZimmetHareketi(personelKodu, hareketId);
    }

    @PostMapping("/zimmetler")
    @ResponseStatus(HttpStatus.CREATED)
    public PersonelZimmetGecmisiResponse zimmetOlustur(
        @PathVariable String personelKodu,
        @Valid @RequestBody PersonelZimmetCreateRequest request
    ) {
        return personelZimmetService.zimmetOlustur(personelKodu, request);
    }

    @PostMapping("/zimmetler/{envanterId}/iade")
    @ResponseStatus(HttpStatus.CREATED)
    public PersonelZimmetGecmisiResponse iadeEt(
        @PathVariable String personelKodu,
        @PathVariable String envanterId,
        @Valid @RequestBody PersonelZimmetIadeRequest request
    ) {
        return personelZimmetService.iadeEt(personelKodu, envanterId, request);
    }
}
