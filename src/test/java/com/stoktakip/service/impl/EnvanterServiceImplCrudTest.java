package com.stoktakip.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stoktakip.dto.EnvanterCreateRequest;
import com.stoktakip.dto.EnvanterDetayCreateRequest;
import com.stoktakip.entity.Depo;
import com.stoktakip.entity.DepoZimmetHareket;
import com.stoktakip.entity.Envanter;
import com.stoktakip.entity.EnvanterDetay;
import com.stoktakip.entity.Firma;
import com.stoktakip.entity.Ihale;
import com.stoktakip.enums.EnvanterTuru;
import com.stoktakip.enums.ZimmetHareketTipi;
import com.stoktakip.exception.BusinessRuleException;
import com.stoktakip.exception.ResourceNotFoundException;
import com.stoktakip.repository.DepoRepository;
import com.stoktakip.repository.DepoZimmetHareketRepository;
import com.stoktakip.repository.EnvanterDetayRepository;
import com.stoktakip.repository.EnvanterRepository;
import com.stoktakip.repository.FirmaRepository;
import com.stoktakip.repository.IhaleRepository;
import com.stoktakip.repository.ModelRepository;
import com.stoktakip.repository.PersonelZimmetHareketRepository;
import java.time.LocalDateTime;
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
    @Mock EnvanterDetayRepository detayRepository;
    @Mock FirmaRepository firmaRepository;
    @Mock IhaleRepository ihaleRepository;
    @Mock DepoZimmetHareketRepository depoHareketRepository;

    private EnvanterServiceImpl service;

    @BeforeEach
    void setUp() {
        lenient().when(envanterRepository.save(any(Envanter.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(detayRepository.save(any(EnvanterDetay.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        service = new EnvanterServiceImpl(
            envanterRepository, personelHareketRepository, depoRepository, modelRepository,
            detayRepository, firmaRepository, ihaleRepository, depoHareketRepository);
    }

    @Test
    void createEnvanterShouldCreateEnvanter() {
        validBaseReferences();
        var response = service.create(request(null));
        assertThat(response.guid()).isNotBlank();
        assertThat(UUID.fromString(response.guid())).isNotNull();
        verify(envanterRepository).save(any(Envanter.class));
    }

    @Test
    void createEnvanterShouldCreateAndLinkEnvanterDetay() {
        validBaseReferences();
        var response = service.create(request(null));
        ArgumentCaptor<EnvanterDetay> captor = ArgumentCaptor.forClass(EnvanterDetay.class);
        verify(detayRepository).save(captor.capture());
        assertThat(captor.getValue().getEnvanter().getGuid()).isEqualTo(response.guid());
        assertThat(response.detay().envanterGuid()).isEqualTo(response.guid());
    }

    @Test
    void createEnvanterShouldCreateInitialDepoZimmetHareket() {
        Depo depo = validBaseReferences();
        var response = service.create(request(null));
        ArgumentCaptor<DepoZimmetHareket> captor = ArgumentCaptor.forClass(DepoZimmetHareket.class);
        verify(depoHareketRepository).save(captor.capture());
        assertThat(captor.getValue().getEnvanter().getGuid()).isEqualTo(response.guid());
        assertThat(captor.getValue().getGidecegiDepo()).isSameAs(depo);
        assertThat(captor.getValue().getHareketTipi()).isEqualTo(ZimmetHareketTipi.DEPO_TRANSFERI);
        assertThat(captor.getValue().getTur()).isEqualTo("GIRIS");
        assertThat(captor.getValue().getAdet()).isEqualTo(1L);
    }

    @Test
    void createEnvanterShouldGenerateSeparateGuidForDetay() {
        validBaseReferences();
        var response = service.create(request(null));
        assertThat(response.detay().guid()).isNotBlank().isNotEqualTo(response.guid());
        assertThat(UUID.fromString(response.detay().guid())).isNotNull();
    }

    @Test
    void createEnvanterShouldResolveFirmaAndIhale() {
        validBaseReferences();
        Firma firma = firma("firma-1");
        when(firmaRepository.findById("firma-1")).thenReturn(Optional.of(firma));
        when(ihaleRepository.findDetayByGuid("ihale-1"))
            .thenReturn(Optional.of(ihale("ihale-1", firma)));
        var response = service.create(request(detay("ihale-1", "firma-1", null, null)));
        assertThat(response.detay().firmaGuid()).isEqualTo("firma-1");
        assertThat(response.detay().ihaleGuid()).isEqualTo("ihale-1");
    }

    @Test
    void createEnvanterShouldUseIhaleFirmasiAsCanonicalFirma() {
        validBaseReferences();
        Firma firma = firma("firma-1");
        when(ihaleRepository.findDetayByGuid("ihale-1"))
            .thenReturn(Optional.of(ihale("ihale-1", firma)));
        var response = service.create(request(detay("ihale-1", null, null, null)));
        assertThat(response.detay().firmaGuid()).isEqualTo("firma-1");
    }

    @Test
    void createEnvanterShouldRejectUnknownFirmaBeforeWrite() {
        validBaseReferences();
        when(firmaRepository.findById("missing")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.create(request(detay(null, "missing", null, null))))
            .isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("Firma bulunamadı");
        verify(envanterRepository, never()).save(any());
    }

    @Test
    void createEnvanterShouldRejectUnknownIhaleBeforeWrite() {
        validBaseReferences();
        when(ihaleRepository.findDetayByGuid("missing")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.create(request(detay("missing", null, null, null))))
            .isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("İhale bulunamadı");
        verify(envanterRepository, never()).save(any());
    }

    @Test
    void createEnvanterShouldRejectInconsistentIhaleFirmaBeforeWrite() {
        validBaseReferences();
        when(ihaleRepository.findDetayByGuid("ihale-1"))
            .thenReturn(Optional.of(ihale("ihale-1", firma("firma-a"))));
        when(firmaRepository.findById("firma-b")).thenReturn(Optional.of(firma("firma-b")));
        assertThatThrownBy(() -> service.create(request(detay("ihale-1", "firma-b", null, null))))
            .isInstanceOf(BusinessRuleException.class).hasMessageContaining("uyuşmuyor");
        verify(envanterRepository, never()).save(any());
    }

    @Test
    void createEnvanterShouldRejectInvalidWarrantyDatesBeforeWrite() {
        validBaseReferences();
        LocalDateTime baslangic = LocalDateTime.of(2029, 1, 1, 0, 0);
        LocalDateTime bitis = LocalDateTime.of(2028, 1, 1, 0, 0);
        assertThatThrownBy(() -> service.create(request(detay(null, null, baslangic, bitis))))
            .isInstanceOf(BusinessRuleException.class).hasMessageContaining("başlangıç tarihinden önce");
        verify(envanterRepository, never()).save(any());
    }

    @Test
    void createEnvanterShouldStopWhenDetailSaveFails() {
        validBaseReferences();
        when(detayRepository.save(any())).thenThrow(new IllegalStateException("detay yazılamadı"));
        assertThatThrownBy(() -> service.create(request(null)))
            .isInstanceOf(IllegalStateException.class).hasMessage("detay yazılamadı");
        verify(envanterRepository).save(any(Envanter.class));
        verify(depoHareketRepository, never()).save(any());
    }

    @Test
    void createEnvanterShouldPropagateDepotMovementFailureAfterDetailSave() {
        validBaseReferences();
        when(depoHareketRepository.save(any()))
            .thenThrow(new IllegalStateException("hareket yazılamadı"));
        assertThatThrownBy(() -> service.create(request(null)))
            .isInstanceOf(IllegalStateException.class).hasMessage("hareket yazılamadı");
        verify(envanterRepository).save(any(Envanter.class));
        verify(detayRepository).save(any(EnvanterDetay.class));
    }

    private Depo validBaseReferences() {
        Depo depo = depo();
        lenient().when(depoRepository.findById("depo-1")).thenReturn(Optional.of(depo));
        return depo;
    }

    private static EnvanterCreateRequest request(EnvanterDetayCreateRequest detay) {
        return new EnvanterCreateRequest(
            "Laptop", EnvanterTuru.DEMIRBAS, "depo-1", null, 10L, "seri", "barkod",
            1L, "aciklama", "birim", detay);
    }

    private static EnvanterDetayCreateRequest detay(
        String ihaleGuid, String firmaGuid, LocalDateTime baslangic, LocalDateTime bitis
    ) {
        return new EnvanterDetayCreateRequest(
            1L, "IHALE", "SYM-1", ihaleGuid, firmaGuid, baslangic, bitis);
    }

    private static Depo depo() {
        Depo depo = new Depo();
        depo.setGuid("depo-1");
        depo.setAd("Ana Depo");
        return depo;
    }

    private static Firma firma(String guid) {
        Firma firma = new Firma();
        firma.setGuid(guid);
        firma.setAd(guid);
        return firma;
    }

    private static Ihale ihale(String guid, Firma firma) {
        Ihale ihale = new Ihale();
        ihale.setGuid(guid);
        ihale.setAd(guid);
        ihale.setFirma(firma);
        return ihale;
    }
}
