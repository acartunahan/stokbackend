package com.stoktakip.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stoktakip.dto.AparatCreateRequest;
import com.stoktakip.dto.MarkaCreateRequest;
import com.stoktakip.entity.Aparat;
import com.stoktakip.entity.Envanter;
import com.stoktakip.enums.EnvanterTuru;
import com.stoktakip.exception.ResourceNotFoundException;
import com.stoktakip.repository.AparatRepository;
import com.stoktakip.repository.EnvanterRepository;
import com.stoktakip.repository.MarkaRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CatalogServicesTest {
    @Mock EnvanterRepository envanterRepository;
    @Mock MarkaRepository markaRepository;
    @Mock AparatRepository aparatRepository;

    @Test
    void sarfListesiRepositorySeviyesindeFiltrelenir() {
        Envanter sarf = new Envanter();
        sarf.setGuid("env-1");
        sarf.setEnvanterAdi("Toner");
        sarf.setEnvanterTuru(EnvanterTuru.SARF);
        when(envanterRepository.findByEnvanterTuru(EnvanterTuru.SARF)).thenReturn(List.of(sarf));

        var result = new EnvanterServiceImpl(envanterRepository).getSarflar();

        assertThat(result).singleElement().extracting("guid").isEqualTo("env-1");
        verify(envanterRepository).findByEnvanterTuru(EnvanterTuru.SARF);
    }

    @Test
    void markaOlustururkenGercekStringGuidUretilir() {
        when(markaRepository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(i -> i.getArgument(0));

        var result = new MarkaServiceImpl(markaRepository)
            .create(new MarkaCreateRequest(" Lenovo ", "Kurumsal"));

        assertThat(result.guid()).isNotBlank();
        assertThat(result.markaAdi()).isEqualTo("Lenovo");
    }

    @Test
    void aparatOlusturmaOlmayanEnvanterIcinKontrolluHataVerir() {
        when(envanterRepository.findById("yok")).thenReturn(Optional.empty());
        var service = new AparatServiceImpl(aparatRepository, envanterRepository);

        assertThatThrownBy(() -> service.create(new AparatCreateRequest("yok", "Adaptör", null)))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Envanter bulunamadı");
    }
}
