package com.stoktakip.controller;

import com.stoktakip.dto.DepoTransferTalebi;
import com.stoktakip.dto.IadeTalebi;
import com.stoktakip.dto.ZimmetHareketYaniti;
import com.stoktakip.dto.ZimmetTalebi;
import com.stoktakip.dto.TopluDepoTransferRequest;
import com.stoktakip.dto.TopluIslemResponse;
import com.stoktakip.service.ZimmetService;
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
@RequestMapping("/api/zimmet")
@Tag(name = "Zimmet İşlemleri", description = "Envanterlerin personel zimmeti, iadesi ve depolar arası transfer işlemleri")
public class ZimmetController {

    private final ZimmetService zimmetService;

    public ZimmetController(ZimmetService zimmetService) {
        this.zimmetService = zimmetService;
    }

    @PostMapping("/personel")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "assignInventoryToPersonnel", summary = "Envanteri Personele Zimmetle",
        description = "Depoda bulunan envanteri talepte belirtilen personele zimmetler ve personel hareket kaydı oluşturur.")
    public ZimmetHareketYaniti personeleZimmetle(@Valid @RequestBody ZimmetTalebi talep) {
        return zimmetService.personeleZimmetle(talep);
    }

    @PostMapping("/iade")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "returnAssignedInventory", summary = "Zimmetli Envanteri İade Al",
        description = "Aktif personel zimmeti bulunan envanteri belirtilen depoya iade alır ve hareket kayıtlarını oluşturur.")
    public ZimmetHareketYaniti iadeAl(@Valid @RequestBody IadeTalebi talep) {
        return zimmetService.iadeAl(talep);
    }

    @PostMapping("/depo-transferi")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "transferInventoryBetweenWarehouses",
        summary = "Envanteri Depolar Arasında Transfer Et",
        description = "Depoda bulunan tek bir envanteri mevcut deposundan belirtilen hedef depoya transfer eder.")
    public ZimmetHareketYaniti depoyaTasi(@Valid @RequestBody DepoTransferTalebi talep) {
        return zimmetService.depoyaTasi(talep);
    }

    @PostMapping("/toplu-depo-transferi")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "transferInventoriesBetweenWarehousesBulk",
        summary = "Depolar Arası Toplu Envanter Transferi Yap",
        description = "Birden fazla envanteri kaynak depodan hedef depoya tek transaction içerisinde transfer eder; bir kalem başarısız olursa işlemin tamamı geri alınır.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Envanterlerin tamamı başarıyla transfer edildi"),
        @ApiResponse(responseCode = "404", description = "Depo veya envanter bulunamadı"),
        @ApiResponse(responseCode = "422", description = "Talep transfer kurallarına uygun değil")
    })
    public TopluIslemResponse<ZimmetHareketYaniti> topluDepoyaTasi(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Kaynak ve hedef depo ile transfer edilecek envanterler", required = true)
        @Valid @RequestBody TopluDepoTransferRequest talep
    ) {
        return zimmetService.topluDepoyaTasi(talep);
    }

    @GetMapping("/envanter/{guid}/personel-hareketleri")
    @Operation(operationId = "getInventoryPersonnelAssignmentHistory",
        summary = "Envanter Personel Zimmet Geçmişini Getir",
        description = "Belirtilen envanterin personel zimmet ve iade hareketlerini en yeniden eskiye getirir.")
    public List<ZimmetHareketYaniti> personelGecmisi(
        @Parameter(description = "Personel zimmet geçmişi getirilecek envanterin benzersiz GUID değeri")
        @PathVariable String guid
    ) {
        return zimmetService.personelGecmisi(guid);
    }

    @GetMapping("/envanter/{guid}/depo-hareketleri")
    @Operation(operationId = "getInventoryWarehouseAssignmentHistory",
        summary = "Envanter Depo Hareket Geçmişini Getir",
        description = "Belirtilen envanterin depo giriş, iade ve transfer hareketlerini en yeniden eskiye getirir.")
    public List<ZimmetHareketYaniti> depoGecmisi(
        @Parameter(description = "Depo hareket geçmişi getirilecek envanterin benzersiz GUID değeri")
        @PathVariable String guid
    ) {
        return zimmetService.depoGecmisi(guid);
    }
}
