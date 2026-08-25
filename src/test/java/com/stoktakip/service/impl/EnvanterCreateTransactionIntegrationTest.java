package com.stoktakip.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import com.stoktakip.dto.EnvanterCreateRequest;
import com.stoktakip.entity.Depo;
import com.stoktakip.enums.EnvanterTuru;
import com.stoktakip.repository.DepoRepository;
import com.stoktakip.repository.DepoZimmetHareketRepository;
import com.stoktakip.repository.EnvanterDetayRepository;
import com.stoktakip.repository.EnvanterRepository;
import com.stoktakip.service.EnvanterService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@SpringBootTest
class EnvanterCreateTransactionIntegrationTest {

    @Autowired EnvanterService service;
    @Autowired EnvanterRepository envanterRepository;
    @Autowired DepoRepository depoRepository;

    @MockitoSpyBean EnvanterDetayRepository detayRepository;
    @MockitoSpyBean DepoZimmetHareketRepository depoHareketRepository;

    @Test
    void createEnvanterShouldPersistEnvanterDetailAndInitialMovementTogether() {
        String depoGuid = yeniDepo();

        var response = service.create(request(depoGuid));

        assertThat(envanterRepository.existsById(response.guid())).isTrue();
        assertThat(detayRepository.existsById(response.detay().guid())).isTrue();
        assertThat(depoHareketRepository.existsByEnvanterGuid(response.guid())).isTrue();
        assertThat(response.detay().envanterGuid()).isEqualTo(response.guid());
    }

    @Test
    void createEnvanterShouldRollbackEverythingWhenDetailSaveFails() {
        String depoGuid = yeniDepo();
        long envanterCount = envanterRepository.count();
        long detayCount = detayRepository.count();
        long hareketCount = depoHareketRepository.count();
        doThrow(new IllegalStateException("detay yazılamadı"))
            .when(detayRepository).save(any());

        assertThatThrownBy(() -> service.create(request(depoGuid)))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("detay yazılamadı");

        assertThat(envanterRepository.count()).isEqualTo(envanterCount);
        assertThat(detayRepository.count()).isEqualTo(detayCount);
        assertThat(depoHareketRepository.count()).isEqualTo(hareketCount);
    }

    @Test
    void createEnvanterShouldRollbackEverythingWhenDepotMovementFails() {
        String depoGuid = yeniDepo();
        long envanterCount = envanterRepository.count();
        long detayCount = detayRepository.count();
        long hareketCount = depoHareketRepository.count();
        doThrow(new IllegalStateException("hareket yazılamadı"))
            .when(depoHareketRepository).save(any());

        assertThatThrownBy(() -> service.create(request(depoGuid)))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("hareket yazılamadı");

        assertThat(envanterRepository.count()).isEqualTo(envanterCount);
        assertThat(detayRepository.count()).isEqualTo(detayCount);
        assertThat(depoHareketRepository.count()).isEqualTo(hareketCount);
    }

    private String yeniDepo() {
        String guid = UUID.randomUUID().toString();
        Depo depo = new Depo();
        depo.setGuid(guid);
        depo.setAd("Transaction test deposu");
        depoRepository.saveAndFlush(depo);
        return guid;
    }

    private static EnvanterCreateRequest request(String depoGuid) {
        return new EnvanterCreateRequest(
            "Transaction test envanteri", EnvanterTuru.DEMIRBAS, depoGuid,
            null, null, null, null, null, null, null, null);
    }
}
