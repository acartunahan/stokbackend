package com.stoktakip.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.stoktakip.dto.AparatCreateRequest;
import com.stoktakip.dto.AparatUpdateRequest;
import com.stoktakip.dto.EnvanterAciklamaCreateRequest;
import com.stoktakip.dto.SarfHareketRequest;
import com.stoktakip.dto.SarfStokResponse;
import com.stoktakip.entity.Depo;
import com.stoktakip.entity.DepoZimmetHareket;
import com.stoktakip.entity.Envanter;
import com.stoktakip.entity.EnvanterDetay;
import com.stoktakip.enums.EnvanterTuru;
import com.stoktakip.enums.ZimmetHareketTipi;
import com.stoktakip.exception.BusinessRuleException;
import com.stoktakip.exception.ResourceNotFoundException;
import com.stoktakip.repository.DepoRepository;
import com.stoktakip.repository.DepoZimmetHareketRepository;
import com.stoktakip.repository.EnvanterDetayRepository;
import com.stoktakip.repository.EnvanterRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;

@SpringBootTest
class EksikUclarIntegrationTest {

    @Autowired EnvanterAciklamaService aciklamaService;
    @Autowired SarfService sarfService;
    @Autowired SarfLogService sarfLogService;
    @Autowired RaporService raporService;
    @Autowired AparatService aparatService;
    @Autowired EnvanterRepository envanterRepository;
    @Autowired EnvanterDetayRepository detayRepository;
    @Autowired DepoRepository depoRepository;
    @Autowired DepoZimmetHareketRepository hareketRepository;

    @Test
    void envanterAciklamasiEklenirListelenirVeSilinir() {
        String suffix = suffix();
        Envanter envanter = envanter("not-" + suffix, EnvanterTuru.DEMIRBAS, depo("not-depo-" + suffix));

        var eklenen = aciklamaService.create(envanter.getGuid(),
            new EnvanterAciklamaCreateRequest("  Ekran çizik  ", null, "Ali", "1001"));

        assertThat(eklenen.notMetni()).isEqualTo("Ekran çizik");
        assertThat(aciklamaService.getByEnvanter(envanter.getGuid()))
            .extracting(item -> item.guid()).containsExactly(eklenen.guid());

        aciklamaService.delete(envanter.getGuid(), eklenen.guid());

        assertThat(aciklamaService.getByEnvanter(envanter.getGuid())).isEmpty();
        assertThatThrownBy(() -> aciklamaService.delete(envanter.getGuid(), eklenen.guid()))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void baskaEnvanterinAciklamasiSilinemez() {
        String suffix = suffix();
        Depo depo = depo("not2-depo-" + suffix);
        Envanter birinci = envanter("not2-a-" + suffix, EnvanterTuru.DEMIRBAS, depo);
        Envanter ikinci = envanter("not2-b-" + suffix, EnvanterTuru.DEMIRBAS, depo);
        var aciklama = aciklamaService.create(birinci.getGuid(),
            new EnvanterAciklamaCreateRequest("not", null, null, null));

        assertThatThrownBy(() -> aciklamaService.delete(ikinci.getGuid(), aciklama.guid()))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void sarfGirisCikisStokVeLogCalisir() {
        String suffix = suffix();
        Depo depo = depo("sarf-depo-" + suffix);
        Envanter toner = envanter("sarf-" + suffix, EnvanterTuru.SARF, depo);

        sarfService.giris(new SarfHareketRequest(toner.getGuid(), depo.getGuid(), 10L, "1001", "Alım"));
        var cikis = sarfService.cikis(new SarfHareketRequest(toner.getGuid(), depo.getGuid(), 4L, "1002", null));

        assertThat(cikis.kalanStok()).isEqualTo(6L);
        List<SarfStokResponse> stok = sarfService.getStok(depo.getGuid(), null);
        assertThat(stok).singleElement().satisfies(item -> {
            assertThat(item.girisAdedi()).isEqualTo(10L);
            assertThat(item.cikisAdedi()).isEqualTo(4L);
            assertThat(item.stok()).isEqualTo(6L);
        });
        assertThatThrownBy(() -> sarfService.cikis(
            new SarfHareketRequest(toner.getGuid(), depo.getGuid(), 7L, null, null)))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("Yetersiz");
        assertThat(sarfLogService.search(depo.getGuid(), null, null, null, null, PageRequest.of(0, 20))
            .content()).extracting(item -> item.islem()).containsExactlyInAnyOrder("GIRIS", "CIKIS");
    }

    @Test
    void eskiSistemdenGelenSarfHareketleriStokaDahilEdilir() {
        String suffix = suffix();
        Depo depo = depo("eski-sarf-depo-" + suffix);
        Envanter kagit = envanter("eski-sarf-" + suffix, EnvanterTuru.SARF, depo);
        eskiHareket(kagit, "gelen", 5L, null, depo);
        eskiHareket(kagit, "giden", -2L, depo, null);

        assertThat(sarfService.getStok(null, kagit.getGuid()))
            .singleElement().extracting(SarfStokResponse::stok).isEqualTo(3L);
    }

    @Test
    void demirbasIcinSarfHareketiYapilamaz() {
        String suffix = suffix();
        Depo depo = depo("dem-depo-" + suffix);
        Envanter laptop = envanter("dem-" + suffix, EnvanterTuru.DEMIRBAS, depo);

        assertThatThrownBy(() -> sarfService.giris(
            new SarfHareketRequest(laptop.getGuid(), depo.getGuid(), 1L, null, null)))
            .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void garantisiBitecekEnvanterlerListelenir() {
        String suffix = suffix();
        Depo depo = depo("garanti-depo-" + suffix);
        Envanter yakin = envanter("garanti-yakin-" + suffix, EnvanterTuru.DEMIRBAS, depo);
        Envanter uzak = envanter("garanti-uzak-" + suffix, EnvanterTuru.DEMIRBAS, depo);
        detay(yakin, LocalDate.now().plusDays(10));
        detay(uzak, LocalDate.now().plusDays(400));

        var liste = raporService.getGarantisiBitecekler(30);

        assertThat(liste).extracting(item -> item.envanterGuid())
            .contains(yakin.getGuid()).doesNotContain(uzak.getGuid());
        assertThat(liste).filteredOn(item -> item.envanterGuid().equals(yakin.getGuid()))
            .singleElement().extracting(item -> item.kalanGun()).isEqualTo(10L);
        assertThat(raporService.getOzet().garantisi30GunIcindeBitecek()).isGreaterThanOrEqualTo(1);
        assertThatThrownBy(() -> raporService.getGarantisiBitecekler(-1))
            .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void aparatGuncellenir() {
        String suffix = suffix();
        Envanter envanter = envanter("aparat-" + suffix, EnvanterTuru.DEMIRBAS, depo("aparat-depo-" + suffix));
        var aparat = aparatService.create(new AparatCreateRequest(envanter.getGuid(), "Şarj aleti", null));

        var guncel = aparatService.update(aparat.guid(), new AparatUpdateRequest(" Adaptör ", "65W"));

        assertThat(guncel.aparatAdi()).isEqualTo("Adaptör");
        assertThat(aparatService.getById(aparat.guid()).aciklama()).isEqualTo("65W");
    }

    private Depo depo(String guid) {
        Depo depo = new Depo();
        depo.setGuid(guid);
        depo.setAd(guid);
        return depoRepository.saveAndFlush(depo);
    }

    private Envanter envanter(String guid, EnvanterTuru tur, Depo depo) {
        Envanter envanter = new Envanter();
        envanter.setGuid(guid);
        envanter.setEnvanterAdi(guid);
        envanter.setEnvanterTuru(tur);
        envanter.setDepo(depo);
        return envanterRepository.saveAndFlush(envanter);
    }

    private void detay(Envanter envanter, LocalDate garantiBitis) {
        EnvanterDetay detay = new EnvanterDetay();
        detay.setGuid("detay-" + envanter.getGuid());
        detay.setEnvanter(envanter);
        detay.setGarantiBitisTarihi(garantiBitis.atTime(12, 0));
        detayRepository.saveAndFlush(detay);
    }

    private void eskiHareket(Envanter envanter, String tur, Long adet, Depo mevcut, Depo gidecegi) {
        DepoZimmetHareket hareket = new DepoZimmetHareket();
        hareket.setEnvanter(envanter);
        hareket.setHareketTipi(ZimmetHareketTipi.DEPO_TRANSFERI);
        hareket.setZimmetTuru(EnvanterTuru.SARF.name());
        hareket.setTur(tur);
        hareket.setAdet(adet);
        hareket.setMevcutDepo(mevcut);
        hareket.setGidecegiDepo(gidecegi);
        hareketRepository.saveAndFlush(hareket);
    }

    private static String suffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
