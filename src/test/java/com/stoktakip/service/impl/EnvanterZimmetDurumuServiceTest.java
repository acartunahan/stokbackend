package com.stoktakip.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stoktakip.entity.Envanter;
import com.stoktakip.entity.PersonelZimmetHareket;
import com.stoktakip.enums.EnvanterTuru;
import com.stoktakip.enums.ZimmetHareketTipi;
import com.stoktakip.exception.BusinessRuleException;
import com.stoktakip.exception.ResourceNotFoundException;
import com.stoktakip.repository.EnvanterRepository;
import com.stoktakip.repository.PersonelZimmetHareketRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EnvanterZimmetDurumuServiceTest {

    @Mock EnvanterRepository envanterRepository;
    @Mock PersonelZimmetHareketRepository hareketRepository;

    private EnvanterServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new EnvanterServiceImpl(envanterRepository, hareketRepository);
    }

    @Test
    void aktifSonZimmetHareketindekiPersoneliDoner() {
        Envanter envanter = envanter("env-1");
        LocalDateTime tarih = LocalDateTime.of(2026, 8, 24, 10, 30);
        PersonelZimmetHareket hareket = hareket(
            envanter, 42L, "9500207", ZimmetHareketTipi.PERSONELE_ZIMMET, true);
        hareket.setOlusturmaTarihi(tarih);
        when(envanterRepository.findById("env-1")).thenReturn(Optional.of(envanter));
        when(hareketRepository.findFirstByEnvanterGuidOrderByIdDesc("env-1"))
            .thenReturn(Optional.of(hareket));

        var response = service.getZimmetDurumu(" env-1 ");

        assertThat(response.zimmetli()).isTrue();
        assertThat(response.personelKodu()).isEqualTo("9500207");
        assertThat(response.hareketId()).isEqualTo(42L);
        assertThat(response.zimmetTarihi()).isEqualTo(tarih);
        verify(hareketRepository).findFirstByEnvanterGuidOrderByIdDesc("env-1");
    }

    @Test
    void sonHareketIadeIseZimmetsizDoner() {
        Envanter envanter = envanter("env-1");
        when(envanterRepository.findById("env-1")).thenReturn(Optional.of(envanter));
        when(hareketRepository.findFirstByEnvanterGuidOrderByIdDesc("env-1"))
            .thenReturn(Optional.of(hareket(
                envanter, 43L, "9500207", ZimmetHareketTipi.IADE, false)));

        var response = service.getZimmetDurumu("env-1");

        assertThat(response.zimmetli()).isFalse();
        assertThat(response.personelKodu()).isNull();
        assertThat(response.hareketId()).isNull();
    }

    @Test
    void pasifPersonelZimmetiGuncelKabulEdilmez() {
        Envanter envanter = envanter("env-1");
        when(envanterRepository.findById("env-1")).thenReturn(Optional.of(envanter));
        when(hareketRepository.findFirstByEnvanterGuidOrderByIdDesc("env-1"))
            .thenReturn(Optional.of(hareket(
                envanter, 44L, "P-ESKI", ZimmetHareketTipi.PERSONELE_ZIMMET, false)));

        assertThat(service.getZimmetDurumu("env-1").zimmetli()).isFalse();
    }

    @Test
    void hareketYoksaZimmetsizDoner() {
        Envanter envanter = envanter("env-1");
        when(envanterRepository.findById("env-1")).thenReturn(Optional.of(envanter));
        when(hareketRepository.findFirstByEnvanterGuidOrderByIdDesc("env-1"))
            .thenReturn(Optional.empty());

        var response = service.getZimmetDurumu("env-1");

        assertThat(response.zimmetli()).isFalse();
        assertThat(response.envanterGuid()).isEqualTo("env-1");
        assertThat(response.envanterAdi()).isEqualTo("Envanter env-1");
    }

    @Test
    void olmayanEnvanterKontrolluHataVerir() {
        when(envanterRepository.findById("yok")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getZimmetDurumu("yok"))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Envanter bulunamadı");
    }

    @Test
    void bosEnvanterGuidReddedilir() {
        assertThatThrownBy(() -> service.getZimmetDurumu("   "))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("envanterGuid");
    }

    private static Envanter envanter(String guid) {
        Envanter envanter = new Envanter();
        envanter.setGuid(guid);
        envanter.setEnvanterAdi("Envanter " + guid);
        envanter.setEnvanterTuru(EnvanterTuru.DEMIRBAS);
        return envanter;
    }

    private static PersonelZimmetHareket hareket(
        Envanter envanter,
        Long id,
        String personelKodu,
        ZimmetHareketTipi tip,
        boolean aktif
    ) {
        PersonelZimmetHareket hareket = new PersonelZimmetHareket();
        hareket.setId(id);
        hareket.setEnvanter(envanter);
        hareket.setPersonelKodu(personelKodu);
        hareket.setHareketTipi(tip);
        hareket.setAktif(aktif);
        return hareket;
    }
}
