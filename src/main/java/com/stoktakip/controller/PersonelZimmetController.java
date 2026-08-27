package com.stoktakip.controller;

import com.stoktakip.dto.PersonelEnvanterResponse;
import com.stoktakip.dto.PersonelZimmetCreateRequest;
import com.stoktakip.dto.PersonelZimmetGecmisiResponse;
import com.stoktakip.dto.PersonelZimmetIadeRequest;
import com.stoktakip.dto.TopluIslemResponse;
import com.stoktakip.dto.TopluPersonelIadeRequest;
import com.stoktakip.dto.TopluPersonelZimmetRequest;
import com.stoktakip.service.PersonelZimmetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Personel Zimmet", description = "Personel envanterleri, zimmet geçmişi, zimmet ve iade işlemleri")
public class PersonelZimmetController {

    private final PersonelZimmetService personelZimmetService;

    public PersonelZimmetController(PersonelZimmetService personelZimmetService) {
        this.personelZimmetService = personelZimmetService;
    }

    @GetMapping("/envanterler")
    @Operation(operationId = "listEmployeeInventories", summary = "Personelin Envanterlerini Listele",
        description = "Belirtilen personele aktif olarak zimmetli envanter kayıtlarını listeler.")
    public List<PersonelEnvanterResponse> getPersonelEnvanterleri(
        @Parameter(description = "Envanterleri listelenecek personelin kurum kodu") @PathVariable String personelKodu
    ) {
        return personelZimmetService.getPersonelEnvanterleri(personelKodu);
    }

    @GetMapping("/zimmet-gecmisi")
    @Operation(operationId = "getEmployeeAssignmentHistory",
        summary = "Personel Zimmet Geçmişini Getir",
        description = "Belirtilen personelin geçmiş ve aktif envanter zimmet hareketlerini kronolojik olarak getirir.")
    public List<PersonelZimmetGecmisiResponse> getZimmetGecmisi(
        @Parameter(description = "Zimmet geçmişi getirilecek personelin kurum kodu") @PathVariable String personelKodu
    ) {
        return personelZimmetService.getZimmetGecmisi(personelKodu);
    }

    @GetMapping("/zimmet-gecmisi/{hareketId}")
    @Operation(operationId = "getEmployeeAssignmentMovementDetail",
        summary = "Personel Zimmet Hareketi Detayını Getir",
        description = "Belirtilen personele ait tek bir zimmet hareketini hareket kimliğiyle getirir.")
    public PersonelZimmetGecmisiResponse getZimmetHareketi(
        @Parameter(description = "Zimmet hareketinin ait olduğu personelin kurum kodu") @PathVariable String personelKodu,
        @Parameter(description = "Getirilecek zimmet hareketinin sayısal kimliği") @PathVariable Long hareketId
    ) {
        return personelZimmetService.getZimmetHareketi(personelKodu, hareketId);
    }

    @PostMapping("/zimmetler")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "assignInventoryToEmployee", summary = "Personele Envanter Zimmeti Yap",
        description = "Depoda bulunan tek bir envanteri belirtilen personele transaction içinde zimmetler.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Envanter personele başarıyla zimmetlendi"),
        @ApiResponse(responseCode = "404", description = "Envanter bulunamadı"),
        @ApiResponse(responseCode = "422", description = "Envanter zimmete uygun durumda değil")
    })
    public PersonelZimmetGecmisiResponse zimmetOlustur(
        @Parameter(description = "Envanterin zimmetleneceği personelin kurum kodu") @PathVariable String personelKodu,
        @Valid @RequestBody PersonelZimmetCreateRequest request
    ) {
        return personelZimmetService.zimmetOlustur(personelKodu, request);
    }

    @PostMapping("/zimmetler/toplu")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "assignInventoriesToEmployeeBulk",
        summary = "Personele Toplu Envanter Zimmeti Yap",
        description = "Belirtilen personele birden fazla envanter kaydını tek transaction içerisinde zimmetler; bir kalem başarısız olursa işlemin tamamı geri alınır.")
    public TopluIslemResponse<PersonelZimmetGecmisiResponse> topluZimmetOlustur(
        @Parameter(description = "Envanterlerin zimmetleneceği personelin kurum kodu") @PathVariable String personelKodu,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Zimmetlenecek envanterler ile işlem açıklaması", required = true)
        @Valid @RequestBody TopluPersonelZimmetRequest request
    ) {
        return personelZimmetService.topluZimmetOlustur(personelKodu, request);
    }

    @PostMapping("/iadeler/toplu")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "returnEmployeeInventoriesBulk",
        summary = "Personelden Toplu Envanter İadesi Al",
        description = "Belirtilen personelin birden fazla zimmetli envanterini hedef depoya tek transaction içerisinde iade alır; bir kalem başarısız olursa işlemin tamamı geri alınır.")
    public TopluIslemResponse<PersonelZimmetGecmisiResponse> topluIadeEt(
        @Parameter(description = "İade alınacak envanterlerin zimmetli olduğu personelin kurum kodu") @PathVariable String personelKodu,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "İade alınacak envanterler, hedef depo ve işlem açıklaması", required = true)
        @Valid @RequestBody TopluPersonelIadeRequest request
    ) {
        return personelZimmetService.topluIadeEt(personelKodu, request);
    }

    @PostMapping("/zimmetler/{envanterId}/iade")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "returnEmployeeInventory", summary = "Personelden Envanter İadesi Al",
        description = "Belirtilen personele aktif olarak zimmetli tek bir envanteri hedef depoya iade alır.")
    public PersonelZimmetGecmisiResponse iadeEt(
        @Parameter(description = "İade alınacak envanterin zimmetli olduğu personelin kurum kodu") @PathVariable String personelKodu,
        @Parameter(description = "İade alınacak envanterin benzersiz GUID değeri") @PathVariable String envanterId,
        @Valid @RequestBody PersonelZimmetIadeRequest request
    ) {
        return personelZimmetService.iadeEt(personelKodu, envanterId, request);
    }
}
