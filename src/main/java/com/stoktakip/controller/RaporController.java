package com.stoktakip.controller;

import com.stoktakip.dto.GarantiBitenResponse;
import com.stoktakip.dto.RaporOzetResponse;
import com.stoktakip.service.RaporService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/raporlar")
@Tag(name = "Rapor", description = "Özet sayılar ve takip raporları")
public class RaporController {
    private final RaporService service;

    public RaporController(RaporService service) {
        this.service = service;
    }

    @GetMapping("/ozet")
    @Operation(operationId = "getDashboardSummary", summary = "Özet Bilgileri Getir",
        description = "Envanter, depo, açık arıza ve yaklaşan garanti bitişi sayılarını getirir.")
    public RaporOzetResponse getOzet() { return service.getOzet(); }

    @GetMapping("/garantisi-bitecekler")
    @Operation(operationId = "listExpiringWarranties", summary = "Garantisi Bitecek Envanterleri Listele",
        description = "Garantisi bugünden itibaren verilen gün sayısı içinde bitecek envanterleri bitiş tarihine göre listeler.")
    public List<GarantiBitenResponse> getGarantisiBitecekler(
        @Parameter(description = "Bugünden itibaren kaç gün içinde biteceği") @RequestParam(defaultValue = "30") int gun
    ) { return service.getGarantisiBitecekler(gun); }
}
