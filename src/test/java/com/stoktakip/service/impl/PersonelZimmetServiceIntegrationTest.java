package com.stoktakip.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.stoktakip.dto.PersonelZimmetCreateRequest;
import com.stoktakip.dto.PersonelZimmetIadeRequest;
import com.stoktakip.entity.Aparat;
import com.stoktakip.entity.Envanter;
import com.stoktakip.entity.PersonelZimmetHareket;
import com.stoktakip.enums.EnvanterTuru;
import com.stoktakip.enums.ZimmetHareketTipi;
import com.stoktakip.repository.AparatRepository;
import com.stoktakip.repository.EnvanterRepository;
import com.stoktakip.repository.PersonelZimmetHareketRepository;
import com.stoktakip.service.PersonelZimmetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class PersonelZimmetServiceIntegrationTest {

    @Autowired PersonelZimmetService service;
    @Autowired EnvanterRepository envanterRepository;
    @Autowired PersonelZimmetHareketRepository hareketRepository;
    @Autowired AparatRepository aparatRepository;

    @Test
    void currentStateSonHarekettenHesaplanirVeHistoryAppendOnlyKalir() {
        Envanter envanter = new Envanter();
        envanter.setGuid("integration-envanter");
        envanter.setEnvanterAdi("Integration Laptop");
        envanter.setEnvanterTuru(EnvanterTuru.DEMIRBAS);
        envanterRepository.saveAndFlush(envanter);

        service.zimmetOlustur(
            "P-1", new PersonelZimmetCreateRequest(envanter.getGuid(), null, "operator"));
        assertThat(service.getPersonelEnvanterleri("P-1"))
            .extracting(response -> response.envanterId())
            .containsExactly(envanter.getGuid());

        service.iadeEt(
            "P-1", envanter.getGuid(), new PersonelZimmetIadeRequest(null, "operator"));
        assertThat(service.getPersonelEnvanterleri("P-1")).isEmpty();
        assertThat(service.getZimmetGecmisi("P-1"))
            .extracting(response -> response.hareketTipi())
            .containsExactly(ZimmetHareketTipi.IADE, ZimmetHareketTipi.PERSONELE_ZIMMET);

        service.zimmetOlustur(
            "P-2", new PersonelZimmetCreateRequest(envanter.getGuid(), null, "operator"));
        assertThat(service.getPersonelEnvanterleri("P-1")).isEmpty();
        assertThat(service.getPersonelEnvanterleri("P-2"))
            .extracting(response -> response.envanterId())
            .containsExactly(envanter.getGuid());

        var tumHareketler = hareketRepository.findByEnvanterGuidOrderByIdDesc(envanter.getGuid());
        assertThat(tumHareketler).hasSize(3);
        assertThat(tumHareketler.get(2).getHareketTipi())
            .isEqualTo(ZimmetHareketTipi.PERSONELE_ZIMMET);
        assertThat(tumHareketler.get(2).getAktif()).isTrue();
    }

    @Test
    void aparatHareketleriPersonelGecmisindeKaybolmaz() {
        Envanter envanter = new Envanter();
        envanter.setGuid("aparat-ana-envanter");
        envanter.setEnvanterAdi("Masaustu Bilgisayar");
        envanter.setEnvanterTuru(EnvanterTuru.DEMIRBAS);
        envanterRepository.saveAndFlush(envanter);

        Aparat aparat = new Aparat();
        aparat.setGuid("aparat-1");
        aparat.setAparatAdi("Klavye");
        aparat.setEnvanter(envanter);
        aparatRepository.saveAndFlush(aparat);

        PersonelZimmetHareket hareket = new PersonelZimmetHareket();
        hareket.setAparat(aparat);
        hareket.setPersonelKodu("P-APARAT");
        hareket.setHareketTipi(ZimmetHareketTipi.PERSONELE_ZIMMET);
        hareket.setZimmetTuru(EnvanterTuru.DEMIRBAS.name());
        hareket.setAktif(true);
        hareketRepository.saveAndFlush(hareket);

        assertThat(service.getZimmetGecmisi("P-APARAT"))
            .singleElement()
            .satisfies(response -> {
                assertThat(response.envanterId()).isEqualTo(envanter.getGuid());
                assertThat(response.aparatId()).isEqualTo(aparat.getGuid());
                assertThat(response.aparatAdi()).isEqualTo("Klavye");
            });
    }
}
