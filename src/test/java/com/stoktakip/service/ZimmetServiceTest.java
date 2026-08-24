package com.stoktakip.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.stoktakip.dto.DepoTransferTalebi;
import com.stoktakip.dto.IadeTalebi;
import com.stoktakip.dto.ZimmetTalebi;
import com.stoktakip.entity.Depo;
import com.stoktakip.entity.DepoZimmetHareket;
import com.stoktakip.entity.Envanter;
import com.stoktakip.entity.PersonelZimmetHareket;
import com.stoktakip.enums.EnvanterTuru;
import com.stoktakip.enums.ZimmetHareketTipi;
import com.stoktakip.exception.BusinessRuleException;
import com.stoktakip.repository.DepoRepository;
import com.stoktakip.repository.DepoZimmetHareketRepository;
import com.stoktakip.repository.EnvanterRepository;
import com.stoktakip.repository.PersonelZimmetHareketRepository;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ZimmetServiceTest {

    @Mock EnvanterRepository envanterRepository;
    @Mock DepoRepository depoRepository;
    @Mock PersonelZimmetHareketRepository personelRepository;
    @Mock DepoZimmetHareketRepository depoHareketRepository;

    private ZimmetService service;

    @BeforeEach
    void setUp() {
        AtomicLong ids = new AtomicLong(1_000_000_000_000L);
        lenient().when(personelRepository.save(any())).thenAnswer(invocation -> {
            PersonelZimmetHareket hareket = invocation.getArgument(0);
            hareket.setId(ids.getAndIncrement());
            return hareket;
        });
        lenient().when(depoHareketRepository.save(any())).thenAnswer(invocation -> {
            DepoZimmetHareket hareket = invocation.getArgument(0);
            hareket.setId(ids.getAndIncrement());
            return hareket;
        });
        service = new ZimmetService(envanterRepository, depoRepository,
            personelRepository, depoHareketRepository);
    }

    @Test
    void demirbasPersoneleZimmetlenirVeIadeAlinir() {
        Envanter demirbas = envanter("demirbas-1", EnvanterTuru.DEMIRBAS);
        when(envanterRepository.findByGuidForUpdate("demirbas-1")).thenReturn(Optional.of(demirbas));
        when(personelRepository.findFirstByEnvanterGuidAndAktifTrueOrderByIdDesc("demirbas-1"))
            .thenReturn(Optional.empty());

        var yanit = service.personeleZimmetle(talep("demirbas-1", "P-A"));

        assertThat(yanit.envanterGuid()).isEqualTo("demirbas-1");
        assertThat(yanit.envanterTuru()).isEqualTo("DEMIRBAS");
        assertThat(yanit.personelKodu()).isEqualTo("P-A");
        assertThat(yanit.hareketTipi()).isEqualTo(ZimmetHareketTipi.PERSONELE_ZIMMET);

        PersonelZimmetHareket aktif = aktifZimmet(demirbas, "P-A");
        when(personelRepository.findFirstByEnvanterGuidAndAktifTrueOrderByIdDesc("demirbas-1"))
            .thenReturn(Optional.of(aktif));
        var iade = service.iadeAl(new IadeTalebi("demirbas-1", null, null, "operator"));

        assertThat(iade.hareketTipi()).isEqualTo(ZimmetHareketTipi.IADE);
        assertThat(aktif.getAktif()).isFalse();
    }

    @Test
    void sarfPersoneleZimmetlenirVeIadeAlinir() {
        Envanter sarf = envanter("sarf-1", EnvanterTuru.SARF);
        when(envanterRepository.findByGuidForUpdate("sarf-1")).thenReturn(Optional.of(sarf));
        when(personelRepository.findFirstByEnvanterGuidAndAktifTrueOrderByIdDesc("sarf-1"))
            .thenReturn(Optional.empty());
        service.personeleZimmetle(talep("sarf-1", "P-S"));

        PersonelZimmetHareket aktif = aktifZimmet(sarf, "P-S");
        when(personelRepository.findFirstByEnvanterGuidAndAktifTrueOrderByIdDesc("sarf-1"))
            .thenReturn(Optional.of(aktif));
        var iade = service.iadeAl(new IadeTalebi("sarf-1", null, null, "operator"));

        assertThat(iade.envanterTuru()).isEqualTo("SARF");
        assertThat(iade.hareketTipi()).isEqualTo(ZimmetHareketTipi.IADE);
        assertThat(aktif.getAktif()).isFalse();
    }

    @Test
    void ayniEnvanterEszamanliIkinciAktifZimmeteVerilemez() {
        Envanter envanter = envanter("envanter-2", EnvanterTuru.DEMIRBAS);
        when(envanterRepository.findByGuidForUpdate("envanter-2")).thenReturn(Optional.of(envanter));
        when(personelRepository.findFirstByEnvanterGuidAndAktifTrueOrderByIdDesc("envanter-2"))
            .thenReturn(Optional.of(aktifZimmet(envanter, "PERSONEL-A")));

        assertThatThrownBy(() -> service.personeleZimmetle(talep("envanter-2", "PERSONEL-B")))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("zaten aktif");
    }

    @Test
    void envanterDepolarArasindaTasinabilir() {
        Envanter envanter = envanter("envanter-depo", EnvanterTuru.DEMIRBAS);
        Depo eskiDepo = depo("depo-a");
        Depo yeniDepo = depo("depo-b");
        envanter.setDepo(eskiDepo);
        when(envanterRepository.findByGuidForUpdate("envanter-depo")).thenReturn(Optional.of(envanter));
        when(personelRepository.findFirstByEnvanterGuidAndAktifTrueOrderByIdDesc("envanter-depo"))
            .thenReturn(Optional.empty());
        when(depoRepository.findById("depo-b")).thenReturn(Optional.of(yeniDepo));

        var yanit = service.depoyaTasi(new DepoTransferTalebi(
            "envanter-depo", "depo-b", null, "operator"));

        assertThat(yanit.hareketTipi()).isEqualTo(ZimmetHareketTipi.DEPO_TRANSFERI);
        assertThat(yanit.mevcutDepoGuid()).isEqualTo("depo-a");
        assertThat(yanit.gidecegiDepoGuid()).isEqualTo("depo-b");
        assertThat(envanter.getDepo()).isSameAs(yeniDepo);
    }

    private static ZimmetTalebi talep(String guid, String personel) {
        return new ZimmetTalebi(guid, personel, null, "operator");
    }

    private static Envanter envanter(String guid, EnvanterTuru tur) {
        Envanter envanter = new Envanter();
        envanter.setGuid(guid);
        envanter.setEnvanterTuru(tur);
        envanter.setEnvanterAdi(guid);
        return envanter;
    }

    private static Depo depo(String guid) {
        Depo depo = new Depo();
        depo.setGuid(guid);
        depo.setAd(guid);
        return depo;
    }

    private static PersonelZimmetHareket aktifZimmet(Envanter envanter, String personel) {
        PersonelZimmetHareket hareket = new PersonelZimmetHareket();
        hareket.setId(99L);
        hareket.setEnvanter(envanter);
        hareket.setPersonelKodu(personel);
        hareket.setAktif(true);
        return hareket;
    }
}
