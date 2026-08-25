package com.stoktakip.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stoktakip.dto.DepoCreateRequest;
import com.stoktakip.dto.DepoUpdateRequest;
import com.stoktakip.dto.FirmaCreateRequest;
import com.stoktakip.dto.FirmaUpdateRequest;
import com.stoktakip.dto.IhaleCreateRequest;
import com.stoktakip.dto.IhaleUpdateRequest;
import com.stoktakip.entity.Depo;
import com.stoktakip.entity.Firma;
import com.stoktakip.entity.Ihale;
import com.stoktakip.enums.EnvanterTuru;
import com.stoktakip.exception.BusinessRuleException;
import com.stoktakip.exception.ResourceNotFoundException;
import com.stoktakip.repository.DepoRepository;
import com.stoktakip.repository.FirmaRepository;
import com.stoktakip.repository.IhaleRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class DepoFirmaIhaleCrudServiceTest {
    @Mock DepoRepository depoRepository;
    @Mock FirmaRepository firmaRepository;
    @Mock IhaleRepository ihaleRepository;

    private DepoServiceImpl depoService;
    private FirmaServiceImpl firmaService;
    private IhaleServiceImpl ihaleService;

    @BeforeEach
    void setUp() {
        depoService = new DepoServiceImpl(depoRepository);
        firmaService = new FirmaServiceImpl(firmaRepository);
        ihaleService = new IhaleServiceImpl(ihaleRepository, firmaRepository);
    }

    @Test
    void createDepoBackendGuidUretir() {
        when(depoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var response = depoService.create(new DepoCreateRequest(" Ana Depo ", "A", "P-1", "AKTIF"));
        assertThat(UUID.fromString(response.guid())).isNotNull();
        assertThat(response.ad()).isEqualTo("Ana Depo");
    }

    @Test
    void getVeUpdateDepoMevcutKaydiKullanir() {
        Depo depo = depo("depo-1", "Eski");
        when(depoRepository.findById("depo-1")).thenReturn(Optional.of(depo));
        when(depoRepository.save(depo)).thenReturn(depo);
        assertThat(depoService.getById("depo-1").ad()).isEqualTo("Eski");
        assertThat(depoService.update("depo-1", new DepoUpdateRequest(
            "Yeni", null, null, "PASIF")).ad()).isEqualTo("Yeni");
    }

    @Test
    void deleteDepoIliskiIhlaliniBusinessRuleYapar() {
        Depo depo = depo("depo-1", "Depo");
        when(depoRepository.findById("depo-1")).thenReturn(Optional.of(depo));
        org.mockito.Mockito.doThrow(new DataIntegrityViolationException("fk"))
            .when(depoRepository).flush();
        assertThatThrownBy(() -> depoService.delete("depo-1"))
            .isInstanceOf(BusinessRuleException.class).hasMessageContaining("kullanımda");
    }

    @Test
    void createFirmaBackendGuidUretir() {
        when(firmaRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var response = firmaService.create(firmaCreate(" Firma A "));
        assertThat(UUID.fromString(response.guid())).isNotNull();
        assertThat(response.ad()).isEqualTo("Firma A");
        assertThat(response.kaynakAlani()).isEqualTo(EnvanterTuru.DEMIRBAS);
    }

    @Test
    void getVeUpdateFirmaMevcutKaydiKullanir() {
        Firma firma = firma("firma-1", "Eski");
        when(firmaRepository.findById("firma-1")).thenReturn(Optional.of(firma));
        when(firmaRepository.save(firma)).thenReturn(firma);
        assertThat(firmaService.getById("firma-1").ad()).isEqualTo("Eski");
        assertThat(firmaService.update("firma-1", firmaUpdate("Yeni")).ad()).isEqualTo("Yeni");
    }

    @Test
    void deleteFirmaIliskiIhlaliniBusinessRuleYapar() {
        Firma firma = firma("firma-1", "Firma");
        when(firmaRepository.findById("firma-1")).thenReturn(Optional.of(firma));
        org.mockito.Mockito.doThrow(new DataIntegrityViolationException("fk"))
            .when(firmaRepository).flush();
        assertThatThrownBy(() -> firmaService.delete("firma-1"))
            .isInstanceOf(BusinessRuleException.class).hasMessageContaining("kullanımda");
    }

    @Test
    void createIhaleBackendGuidUretirVeFirmayiSetEder() {
        Firma firma = firma("firma-1", "Firma A");
        when(firmaRepository.findById("firma-1")).thenReturn(Optional.of(firma));
        when(ihaleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var response = ihaleService.create(ihaleCreate("firma-1", "İhale A"));
        assertThat(UUID.fromString(response.guid())).isNotNull();
        assertThat(response.firmaGuid()).isEqualTo("firma-1");
        assertThat(response.firmaAdi()).isEqualTo("Firma A");
    }

    @Test
    void createIhaleFirmaGuidZorunludur() {
        assertThatThrownBy(() -> ihaleService.create(ihaleCreate(" ", "İhale")))
            .isInstanceOf(BusinessRuleException.class).hasMessageContaining("firmaGuid");
        verify(ihaleRepository, never()).save(any());
    }

    @Test
    void createIhaleBilinmeyenFirmayiReddeder() {
        when(firmaRepository.findById("yok")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> ihaleService.create(ihaleCreate("yok", "İhale")))
            .isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("Firma bulunamadı");
        verify(ihaleRepository, never()).save(any());
    }

    @Test
    void getIhaleFirmaBilgisiniDoner() {
        Ihale ihale = ihale("ihale-1", firma("firma-1", "Firma A"));
        when(ihaleRepository.findDetayByGuid("ihale-1")).thenReturn(Optional.of(ihale));
        var response = ihaleService.getById("ihale-1");
        assertThat(response.firmaGuid()).isEqualTo("firma-1");
        assertThat(response.firmaAdi()).isEqualTo("Firma A");
    }

    @Test
    void updateIhaleYeniFirmayiDogrular() {
        Ihale ihale = ihale("ihale-1", firma("firma-1", "Eski Firma"));
        Firma yeniFirma = firma("firma-2", "Yeni Firma");
        when(ihaleRepository.findDetayByGuid("ihale-1")).thenReturn(Optional.of(ihale));
        when(firmaRepository.findById("firma-2")).thenReturn(Optional.of(yeniFirma));
        when(ihaleRepository.save(ihale)).thenReturn(ihale);
        var response = ihaleService.update("ihale-1", ihaleUpdate("firma-2", "Yeni İhale"));
        assertThat(response.firmaGuid()).isEqualTo("firma-2");
        assertThat(ihale.getFirma()).isSameAs(yeniFirma);
    }

    @Test
    void deleteIhaleFirmayiSilmez() {
        Firma firma = firma("firma-1", "Firma");
        Ihale ihale = ihale("ihale-1", firma);
        when(ihaleRepository.findDetayByGuid("ihale-1")).thenReturn(Optional.of(ihale));
        ihaleService.delete("ihale-1");
        verify(ihaleRepository).delete(ihale);
        verify(firmaRepository, never()).delete(any());
    }

    private static Depo depo(String guid, String ad) {
        Depo depo = new Depo(); depo.setGuid(guid); depo.setAd(ad); return depo;
    }

    private static Firma firma(String guid, String ad) {
        Firma firma = new Firma(); firma.setGuid(guid); firma.setAd(ad);
        firma.setKaynakAlani(EnvanterTuru.DEMIRBAS); return firma;
    }

    private static Ihale ihale(String guid, Firma firma) {
        Ihale ihale = new Ihale(); ihale.setGuid(guid); ihale.setAd("İhale"); ihale.setFirma(firma);
        return ihale;
    }

    private static FirmaCreateRequest firmaCreate(String ad) {
        return new FirmaCreateRequest(ad, null, null, null, null, null, null, null,
            null, null, null, "AKTIF", EnvanterTuru.DEMIRBAS);
    }

    private static FirmaUpdateRequest firmaUpdate(String ad) {
        return new FirmaUpdateRequest(ad, null, null, null, null, null, null, null,
            null, null, null, "AKTIF", EnvanterTuru.DEMIRBAS);
    }

    private static IhaleCreateRequest ihaleCreate(String firmaGuid, String ad) {
        return new IhaleCreateRequest(firmaGuid, ad, null, null, null, null, null, null,
            null, null, null, null, null, "AKTIF");
    }

    private static IhaleUpdateRequest ihaleUpdate(String firmaGuid, String ad) {
        return new IhaleUpdateRequest(firmaGuid, ad, null, null, null, null, null, null,
            null, null, null, null, null, "AKTIF");
    }
}
