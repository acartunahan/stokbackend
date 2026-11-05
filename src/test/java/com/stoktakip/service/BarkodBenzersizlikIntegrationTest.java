package com.stoktakip.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.stoktakip.dto.EnvanterCreateRequest;
import com.stoktakip.dto.EnvanterResponse;
import com.stoktakip.dto.EnvanterUpdateRequest;
import com.stoktakip.entity.Depo;
import com.stoktakip.entity.Envanter;
import com.stoktakip.enums.EnvanterTuru;
import com.stoktakip.exception.BusinessRuleException;
import com.stoktakip.repository.DepoRepository;
import com.stoktakip.repository.EnvanterRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BarkodBenzersizlikIntegrationTest {

    @Autowired EnvanterService envanterService;
    @Autowired EnvanterRepository envanterRepository;
    @Autowired DepoRepository depoRepository;

    @Test
    void ayniBarkodlaIkinciEnvanterEklenemez() {
        String suffix = suffix();
        Depo depo = depo("barkod-depo-" + suffix);
        envanterService.create(createRequest(depo, "BRK-" + suffix));

        assertThatThrownBy(() -> envanterService.create(createRequest(depo, " brk-" + suffix + " ")))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("barkod");
    }

    @Test
    void baskaEnvanterinBarkoduGuncellemeyleAlinamaz() {
        String suffix = suffix();
        Depo depo = depo("barkod-depo2-" + suffix);
        envanterService.create(createRequest(depo, "BRK-A-" + suffix));
        EnvanterResponse ikinci = envanterService.create(createRequest(depo, "BRK-B-" + suffix));

        assertThatThrownBy(() -> envanterService.update(ikinci.guid(), updateRequest("BRK-A-" + suffix)))
            .isInstanceOf(BusinessRuleException.class);
        assertThat(envanterService.update(ikinci.guid(), updateRequest("BRK-B-" + suffix)).barkod())
            .isEqualTo("BRK-B-" + suffix);
    }

    @Test
    void silinenEnvanterinBarkoduTekrarKullanilabilir() {
        String suffix = suffix();
        Depo depo = depo("barkod-depo3-" + suffix);
        Envanter silinen = new Envanter();
        silinen.setGuid("silinen-" + suffix);
        silinen.setEnvanterAdi("Silinen");
        silinen.setEnvanterTuru(EnvanterTuru.DEMIRBAS);
        silinen.setBarkod("BRK-S-" + suffix);
        silinen.setDepo(depo);
        silinen.softDelete();
        envanterRepository.saveAndFlush(silinen);

        assertThat(envanterService.create(createRequest(depo, "BRK-S-" + suffix)).barkod())
            .isEqualTo("BRK-S-" + suffix);
    }

    @Test
    void eskiSistemdenGelenAyniBarkodluKayitBarkoduDegismedenGuncellenebilir() {
        String suffix = suffix();
        Depo depo = depo("barkod-depo4-" + suffix);
        Envanter birinci = eskiKayit("eski-1-" + suffix, "BRK-E-" + suffix, depo);
        eskiKayit("eski-2-" + suffix, "BRK-E-" + suffix, depo);

        assertThat(envanterService.update(birinci.getGuid(), updateRequest("BRK-E-" + suffix)).envanterAdi())
            .isEqualTo("Guncel");
    }

    @Test
    void bosBarkodBenzersizlikKontrolunaTakilmaz() {
        String suffix = suffix();
        Depo depo = depo("barkod-depo5-" + suffix);
        envanterService.create(createRequest(depo, null));

        assertThat(envanterService.create(createRequest(depo, "  ")).barkod()).isNull();
    }

    private Envanter eskiKayit(String guid, String barkod, Depo depo) {
        Envanter envanter = new Envanter();
        envanter.setGuid(guid);
        envanter.setEnvanterAdi(guid);
        envanter.setEnvanterTuru(EnvanterTuru.DEMIRBAS);
        envanter.setBarkod(barkod);
        envanter.setDepo(depo);
        return envanterRepository.saveAndFlush(envanter);
    }

    private Depo depo(String guid) {
        Depo depo = new Depo();
        depo.setGuid(guid);
        depo.setAd(guid);
        return depoRepository.saveAndFlush(depo);
    }

    private static EnvanterCreateRequest createRequest(Depo depo, String barkod) {
        return new EnvanterCreateRequest("Cihaz", EnvanterTuru.DEMIRBAS, depo.getGuid(), null,
            null, null, barkod, null, null, null, null);
    }

    private static EnvanterUpdateRequest updateRequest(String barkod) {
        return new EnvanterUpdateRequest("Guncel", EnvanterTuru.DEMIRBAS, null, null, null, barkod,
            null, null, null);
    }

    private static String suffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
