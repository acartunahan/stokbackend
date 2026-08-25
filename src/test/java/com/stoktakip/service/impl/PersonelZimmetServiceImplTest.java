package com.stoktakip.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stoktakip.dto.PersonelEnvanterResponse;
import com.stoktakip.dto.PersonelZimmetCreateRequest;
import com.stoktakip.dto.PersonelZimmetGecmisiResponse;
import com.stoktakip.dto.PersonelZimmetIadeRequest;
import com.stoktakip.entity.Envanter;
import com.stoktakip.entity.PersonelZimmetHareket;
import com.stoktakip.enums.EnvanterTuru;
import com.stoktakip.enums.ZimmetHareketTipi;
import com.stoktakip.exception.BusinessRuleException;
import com.stoktakip.exception.ResourceNotFoundException;
import com.stoktakip.repository.EnvanterRepository;
import com.stoktakip.repository.PersonelZimmetHareketRepository;
import com.stoktakip.repository.DepoRepository;
import com.stoktakip.repository.DepoZimmetHareketRepository;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PersonelZimmetServiceImplTest {

    @Mock PersonelZimmetHareketRepository hareketRepository;
    @Mock EnvanterRepository envanterRepository;
    @Mock DepoRepository depoRepository;
    @Mock DepoZimmetHareketRepository depoHareketRepository;

    private PersonelZimmetServiceImpl service;

    @BeforeEach
    void setUp() {
        AtomicLong ids = new AtomicLong(100);
        lenient().when(hareketRepository.save(any(PersonelZimmetHareket.class))).thenAnswer(invocation -> {
            PersonelZimmetHareket hareket = invocation.getArgument(0);
            hareket.setId(ids.getAndIncrement());
            return hareket;
        });
        service = new PersonelZimmetServiceImpl(hareketRepository, envanterRepository,
            depoRepository, depoHareketRepository);
    }

    @Test
    void mevcutEnvanterlerSadeceIstenenPersonelKoduIleSorgulanir() {
        PersonelEnvanterResponse response = new PersonelEnvanterResponse(
            "env-1", "Laptop", EnvanterTuru.DEMIRBAS, "Marka", "Model", null);
        when(hareketRepository.findMevcutEnvanterlerByPersonelKodu("P-1"))
            .thenReturn(List.of(response));

        assertThat(service.getPersonelEnvanterleri("  P-1 ")).containsExactly(response);
        verify(hareketRepository).findMevcutEnvanterlerByPersonelKodu("P-1");
        verify(hareketRepository, never()).findAll();
    }

    @Test
    void zimmetGecmisiSadeceIstenenPersonelKoduIleSorgulanir() {
        when(hareketRepository.findGecmisByPersonelKodu("P-1")).thenReturn(List.of());

        assertThat(service.getZimmetGecmisi("P-1")).isEmpty();
        verify(hareketRepository).findGecmisByPersonelKodu("P-1");
        verify(hareketRepository, never()).findAll();
    }

    @Test
    void baskaPersoneleAitHareketDonmez() {
        when(hareketRepository.findGecmisByPersonelKoduAndId("P-1", 42L))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getZimmetHareketi("P-1", 42L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void zimmetOlusturur() {
        Envanter envanter = envanter("env-1");
        when(envanterRepository.findByGuidForUpdate("env-1")).thenReturn(Optional.of(envanter));
        when(hareketRepository.findFirstByEnvanterGuidOrderByIdDesc("env-1"))
            .thenReturn(Optional.empty());

        var response = service.zimmetOlustur(
            "P-1", new PersonelZimmetCreateRequest("env-1", "aciklama", "operator"));

        assertThat(response.hareketTipi()).isEqualTo(ZimmetHareketTipi.PERSONELE_ZIMMET);
        assertThat(response.personelKodu()).isEqualTo("P-1");
        assertThat(response.aktif()).isTrue();
    }

    @Test
    void olmayanEnvantereZimmetOlusturulamaz() {
        when(envanterRepository.findByGuidForUpdate("yok")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.zimmetOlustur(
            "P-1", new PersonelZimmetCreateRequest("yok", null, null)))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void aktifZimmetliEnvanterTekrarZimmetlenemez() {
        Envanter envanter = envanter("env-1");
        when(envanterRepository.findByGuidForUpdate("env-1")).thenReturn(Optional.of(envanter));
        when(hareketRepository.findFirstByEnvanterGuidOrderByIdDesc("env-1"))
            .thenReturn(Optional.of(hareket(envanter, "P-2", ZimmetHareketTipi.PERSONELE_ZIMMET, true)));

        assertThatThrownBy(() -> service.zimmetOlustur(
            "P-1", new PersonelZimmetCreateRequest("env-1", null, null)))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("zaten aktif");
    }

    @Test
    void iadeYeniHareketOlustururVeEskiHareketiDegistirmez() {
        Envanter envanter = envanter("env-1");
        PersonelZimmetHareket eski = hareket(
            envanter, "P-1", ZimmetHareketTipi.PERSONELE_ZIMMET, true);
        when(envanterRepository.findByGuidForUpdate("env-1")).thenReturn(Optional.of(envanter));
        when(hareketRepository.findFirstByEnvanterGuidOrderByIdDesc("env-1"))
            .thenReturn(Optional.of(eski));
        when(depoRepository.findById("depo-1")).thenReturn(Optional.of(depo("depo-1")));

        var response = service.iadeEt(
            "P-1", "env-1", new PersonelZimmetIadeRequest("depo-1", "iade", "operator"));

        assertThat(response.hareketTipi()).isEqualTo(ZimmetHareketTipi.IADE);
        assertThat(response.aktif()).isFalse();
        assertThat(eski.getAktif()).isTrue();
        verify(hareketRepository).save(any(PersonelZimmetHareket.class));
        verify(hareketRepository, never()).delete(any());
    }

    @Test
    void baskaPersonelinZimmetiIadeEdilemez() {
        Envanter envanter = envanter("env-1");
        when(envanterRepository.findByGuidForUpdate("env-1")).thenReturn(Optional.of(envanter));
        when(hareketRepository.findFirstByEnvanterGuidOrderByIdDesc("env-1"))
            .thenReturn(Optional.of(hareket(
                envanter, "P-2", ZimmetHareketTipi.PERSONELE_ZIMMET, true)));

        assertThatThrownBy(() -> service.iadeEt(
            "P-1", "env-1", new PersonelZimmetIadeRequest(null, null)))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("bu personele");
    }

    @Test
    void dahaOnceIadeEdilmisEnvanterTekrarIadeEdilemez() {
        Envanter envanter = envanter("env-1");
        when(envanterRepository.findByGuidForUpdate("env-1")).thenReturn(Optional.of(envanter));
        when(hareketRepository.findFirstByEnvanterGuidOrderByIdDesc("env-1"))
            .thenReturn(Optional.of(hareket(envanter, "P-1", ZimmetHareketTipi.IADE, false)));

        assertThatThrownBy(() -> service.iadeEt(
            "P-1", "env-1", new PersonelZimmetIadeRequest(null, null)))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("aktif personel zimmeti");
    }

    private static Envanter envanter(String id) {
        Envanter envanter = new Envanter();
        envanter.setGuid(id);
        envanter.setEnvanterAdi("Envanter " + id);
        envanter.setEnvanterTuru(EnvanterTuru.DEMIRBAS);
        envanter.setDepo(depo("depo-baslangic"));
        return envanter;
    }

    private static com.stoktakip.entity.Depo depo(String id) {
        com.stoktakip.entity.Depo depo = new com.stoktakip.entity.Depo();
        depo.setGuid(id);
        depo.setAd(id);
        return depo;
    }

    private static PersonelZimmetHareket hareket(
        Envanter envanter,
        String personelKodu,
        ZimmetHareketTipi tip,
        boolean aktif
    ) {
        PersonelZimmetHareket hareket = new PersonelZimmetHareket();
        hareket.setId(10L);
        hareket.setEnvanter(envanter);
        hareket.setPersonelKodu(personelKodu);
        hareket.setHareketTipi(tip);
        hareket.setAktif(aktif);
        return hareket;
    }
}
