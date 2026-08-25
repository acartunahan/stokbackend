package com.stoktakip.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stoktakip.dto.EnvanterCreateRequest;
import com.stoktakip.entity.Depo;
import com.stoktakip.entity.DepoZimmetHareket;
import com.stoktakip.entity.Envanter;
import com.stoktakip.enums.EnvanterTuru;
import com.stoktakip.enums.ZimmetHareketTipi;
import com.stoktakip.repository.DepoRepository;
import com.stoktakip.repository.DepoZimmetHareketRepository;
import com.stoktakip.repository.EnvanterRepository;
import com.stoktakip.repository.ModelRepository;
import com.stoktakip.repository.PersonelZimmetHareketRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EnvanterServiceImplCrudTest {

    @Mock EnvanterRepository envanterRepository;
    @Mock PersonelZimmetHareketRepository personelHareketRepository;
    @Mock DepoRepository depoRepository;
    @Mock ModelRepository modelRepository;
    @Mock DepoZimmetHareketRepository depoHareketRepository;

    private EnvanterServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new EnvanterServiceImpl(envanterRepository, personelHareketRepository,
            depoRepository, modelRepository, depoHareketRepository);
    }

    @Test
    void createEnvanterIlkDepoHareketiniOlusturur() {
        Depo depo = depo();
        when(depoRepository.findById("depo-1")).thenReturn(Optional.of(depo));
        when(envanterRepository.save(any(Envanter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create(request());

        assertThat(response.guid()).isNotBlank();
        assertThat(UUID.fromString(response.guid())).isNotNull();
        assertThat(response.depoGuid()).isEqualTo("depo-1");
        ArgumentCaptor<DepoZimmetHareket> captor = ArgumentCaptor.forClass(DepoZimmetHareket.class);
        verify(depoHareketRepository).save(captor.capture());
        assertThat(captor.getValue().getEnvanter().getGuid()).isEqualTo(response.guid());
        assertThat(captor.getValue().getGidecegiDepo()).isSameAs(depo);
        assertThat(captor.getValue().getHareketTipi()).isEqualTo(ZimmetHareketTipi.DEPO_TRANSFERI);
        assertThat(captor.getValue().getTur()).isEqualTo("GIRIS");
        assertThat(captor.getValue().getAdet()).isEqualTo(1L);
    }

    @Test
    void createEnvanterIlkHareketKaydedilemezseHatayiYukarIletir() {
        when(depoRepository.findById("depo-1")).thenReturn(Optional.of(depo()));
        when(envanterRepository.save(any(Envanter.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(depoHareketRepository.save(any())).thenThrow(new IllegalStateException("hareket yazilamadi"));

        assertThatThrownBy(() -> service.create(request()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("hareket yazilamadi");
        verify(envanterRepository).save(any(Envanter.class));
    }

    private static EnvanterCreateRequest request() {
        return new EnvanterCreateRequest("Laptop", EnvanterTuru.DEMIRBAS,
            "depo-1", null, 10L, "seri", "barkod", 1L, "aciklama", "birim");
    }

    private static Depo depo() {
        Depo depo = new Depo();
        depo.setGuid("depo-1");
        depo.setAd("Ana Depo");
        return depo;
    }
}
