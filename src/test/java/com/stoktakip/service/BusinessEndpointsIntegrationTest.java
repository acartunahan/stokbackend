package com.stoktakip.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.stoktakip.dto.EnvanterFilter;
import com.stoktakip.dto.TopluDepoTransferRequest;
import com.stoktakip.dto.TopluEnvanterItemRequest;
import com.stoktakip.dto.TopluPersonelIadeRequest;
import com.stoktakip.dto.TopluPersonelZimmetRequest;
import com.stoktakip.entity.Depo;
import com.stoktakip.entity.Envanter;
import com.stoktakip.enums.EnvanterTuru;
import com.stoktakip.exception.ResourceNotFoundException;
import com.stoktakip.repository.DepoRepository;
import com.stoktakip.repository.DepoZimmetHareketRepository;
import com.stoktakip.repository.EnvanterRepository;
import com.stoktakip.repository.PersonelZimmetHareketRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@SpringBootTest
class BusinessEndpointsIntegrationTest {

    @Autowired EnvanterService envanterService;
    @Autowired HareketService hareketService;
    @Autowired ZimmetService zimmetService;
    @Autowired PersonelZimmetService personelZimmetService;
    @Autowired EnvanterRepository envanterRepository;
    @Autowired DepoRepository depoRepository;
    @Autowired DepoZimmetHareketRepository depoHareketRepository;
    @Autowired PersonelZimmetHareketRepository personelHareketRepository;

    @Test
    void envanterPaginationFilteringSortingVeLookupCalisir() {
        String suffix = suffix();
        Depo depo = depo("filter-depo-" + suffix);
        envanter("filter-1-" + suffix, "Cihaz C " + suffix, "SER-C-" + suffix,
            "BAR-C-" + suffix, 930003L, depo);
        envanter("filter-2-" + suffix, "Cihaz A " + suffix, "SER-A-" + suffix,
            "BAR-A-" + suffix, 930001L, depo);
        envanter("filter-3-" + suffix, "Cihaz B " + suffix, "SER-B-" + suffix,
            "BAR-B-" + suffix, 930002L, depo);

        EnvanterFilter filter = new EnvanterFilter(
            null, null, null, depo.getGuid(), null, EnvanterTuru.DEMIRBAS,
            null, null, null, "Cihaz");
        assertThat(envanterService.search(new EnvanterFilter(
            null, null, null, null, null, null, null, null, null, null),
            PageRequest.of(0, 100)).content()).extracting(item -> item.guid())
            .contains("filter-1-" + suffix, "filter-2-" + suffix, "filter-3-" + suffix);
        assertThat(envanterService.search(new EnvanterFilter(
            null, null, null, depo.getGuid(), null, null, null, null, null, null),
            PageRequest.of(0, 100)).totalElements()).isEqualTo(3);
        assertThat(envanterService.search(new EnvanterFilter(
            null, null, null, depo.getGuid(), null, EnvanterTuru.DEMIRBAS,
            null, null, null, null), PageRequest.of(0, 100)).totalElements()).isEqualTo(3);
        assertThat(envanterService.search(new EnvanterFilter(
            null, null, null, depo.getGuid(), null, null,
            null, null, null, "Cihaz"), PageRequest.of(0, 100)).totalElements()).isEqualTo(3);
        var firstPage = envanterService.search(filter,
            PageRequest.of(0, 2, Sort.by("demirbasNumarasi").ascending()));
        var secondPage = envanterService.search(filter,
            PageRequest.of(1, 2, Sort.by("demirbasNumarasi").ascending()));

        assertThat(firstPage.content()).extracting(item -> item.demirbasNumarasi())
            .containsExactly(930001L, 930002L);
        assertThat(secondPage.content()).extracting(item -> item.demirbasNumarasi())
            .containsExactly(930003L);
        assertThat(firstPage.totalElements()).isEqualTo(3);
        assertThat(firstPage.totalPages()).isEqualTo(2);
        assertThat(envanterService.getByDepo(depo.getGuid(), filter, PageRequest.of(0, 20))
            .totalElements()).isEqualTo(3);
        assertThat(envanterService.lookup("SER-B-" + suffix, null, null).demirbasNumarasi())
            .isEqualTo(930002L);
        assertThat(envanterService.lookup(null, 930001L, null).barkod())
            .isEqualTo("BAR-A-" + suffix);
        assertThat(envanterService.lookup(null, null, "BAR-C-" + suffix).seriNo())
            .isEqualTo("SER-C-" + suffix);
    }

    @Test
    void transferSonrasiBirlesikTimelineVeIkiDeponunYonleriDogruDoner() {
        String suffix = suffix();
        Depo kaynak = depo("timeline-source-" + suffix);
        Depo hedef = depo("timeline-target-" + suffix);
        Envanter envanter = envanter("timeline-item-" + suffix, "Timeline Item",
            "TL-" + suffix, "TLB-" + suffix, 940001L, kaynak);

        zimmetService.depoyaTasi(new com.stoktakip.dto.DepoTransferTalebi(
            envanter.getGuid(), hedef.getGuid(), "timeline", "tester"));

        var timeline = hareketService.getEnvanterHareketleri(
            envanter.getGuid(), null, null, PageRequest.of(0, 20));
        var kaynakHareketleri = hareketService.getDepoHareketleri(
            kaynak.getGuid(), null, null, PageRequest.of(0, 20));
        var hedefHareketleri = hareketService.getDepoHareketleri(
            hedef.getGuid(), null, null, PageRequest.of(0, 20));

        assertThat(timeline.content()).singleElement().satisfies(hareket -> {
            assertThat(hareket.kaynakTuru()).isEqualTo("DEPO");
            assertThat(hareket.kaynakDepoGuid()).isEqualTo(kaynak.getGuid());
            assertThat(hareket.hedefDepoGuid()).isEqualTo(hedef.getGuid());
        });
        assertThat(kaynakHareketleri.content()).singleElement()
            .extracting(item -> item.hareketYonu()).isEqualTo("CIKIS");
        assertThat(hedefHareketleri.content()).singleElement()
            .extracting(item -> item.hareketYonu()).isEqualTo("GIRIS");
    }

    @Test
    void birlesikTimelineIstenenIdSiralamasiVePaginationUygular() {
        String suffix = suffix();
        Depo kaynak = depo("timeline-sort-source-" + suffix);
        Depo hedef = depo("timeline-sort-target-" + suffix);
        Envanter envanter = envanter("timeline-sort-item-" + suffix, "Timeline Sort",
            null, null, null, kaynak);

        zimmetService.depoyaTasi(new com.stoktakip.dto.DepoTransferTalebi(
            envanter.getGuid(), hedef.getGuid(), "first", "tester"));
        zimmetService.depoyaTasi(new com.stoktakip.dto.DepoTransferTalebi(
            envanter.getGuid(), kaynak.getGuid(), "second", "tester"));
        zimmetService.personeleZimmetle(new com.stoktakip.dto.ZimmetTalebi(
            envanter.getGuid(), "P-" + suffix, "third", "tester"));

        var pages = java.util.stream.IntStream.range(0, 3)
            .mapToObj(page -> hareketService.getEnvanterHareketleri(
                envanter.getGuid(), null, null,
                PageRequest.of(page, 1, Sort.by("id").ascending())))
            .toList();
        var ids = pages.stream()
            .flatMap(page -> page.content().stream())
            .map(item -> item.hareketId())
            .toList();

        assertThat(pages.getFirst().totalElements()).isEqualTo(3);
        assertThat(pages.getFirst().totalPages()).isEqualTo(3);
        assertThat(pages).allSatisfy(page -> assertThat(page.content()).hasSize(1));
        assertThat(ids).isSorted();
    }

    @Test
    void topluTransferBasariliVeGecersizKalemdeAtomicRollbackYapar() {
        String suffix = suffix();
        Depo kaynak = depo("bulk-source-" + suffix);
        Depo hedef = depo("bulk-target-" + suffix);
        Envanter first = envanter("bulk-a-" + suffix, "Bulk A", null, null, null, kaynak);
        Envanter second = envanter("bulk-b-" + suffix, "Bulk B", null, null, null, kaynak);

        var success = zimmetService.topluDepoyaTasi(new TopluDepoTransferRequest(
            kaynak.getGuid(), hedef.getGuid(), items(first, second), "bulk", "tester"));
        assertThat(success.toplamKayit()).isEqualTo(2);
        assertThat(envanterRepository.findById(first.getGuid()).orElseThrow().getDepo().getGuid())
            .isEqualTo(hedef.getGuid());
        assertThat(envanterRepository.findById(second.getGuid()).orElseThrow().getDepo().getGuid())
            .isEqualTo(hedef.getGuid());

        Envanter rollbackCandidate = envanter(
            "rollback-a-" + suffix, "Rollback A", null, null, null, kaynak);
        assertThatThrownBy(() -> zimmetService.topluDepoyaTasi(new TopluDepoTransferRequest(
            kaynak.getGuid(), hedef.getGuid(), List.of(
                item(rollbackCandidate), new TopluEnvanterItemRequest("zz-missing-" + suffix, 1L)),
            "rollback", "tester")))
            .isInstanceOf(ResourceNotFoundException.class);

        assertThat(envanterRepository.findById(rollbackCandidate.getGuid())
            .orElseThrow().getDepo().getGuid()).isEqualTo(kaynak.getGuid());
        assertThat(depoHareketRepository.findByEnvanterGuidOrderByIdDesc(
            rollbackCandidate.getGuid())).isEmpty();
    }

    @Test
    void topluZimmetVeIadeBasariliCalisir() {
        String suffix = suffix();
        Depo kaynak = depo("person-source-" + suffix);
        Depo iadeDeposu = depo("person-return-" + suffix);
        Envanter first = envanter("person-a-" + suffix, "Person A", null, null, null, kaynak);
        Envanter second = envanter("person-b-" + suffix, "Person B", null, null, null, kaynak);
        String personelKodu = "P-" + suffix;

        var assignment = personelZimmetService.topluZimmetOlustur(personelKodu,
            new TopluPersonelZimmetRequest(items(first, second), "teslim", "tester"));
        assertThat(assignment.toplamKayit()).isEqualTo(2);
        assertThat(envanterRepository.findById(first.getGuid()).orElseThrow().getDepo()).isNull();
        assertThat(envanterRepository.findById(second.getGuid()).orElseThrow().getDepo()).isNull();

        var returns = personelZimmetService.topluIadeEt(personelKodu,
            new TopluPersonelIadeRequest(iadeDeposu.getGuid(), items(first, second),
                "iade", "tester"));
        assertThat(returns.toplamKayit()).isEqualTo(2);
        assertThat(envanterRepository.findById(first.getGuid()).orElseThrow().getDepo().getGuid())
            .isEqualTo(iadeDeposu.getGuid());
        assertThat(personelHareketRepository.findByEnvanterGuidOrderByIdDesc(first.getGuid()))
            .hasSize(2);
    }

    @Test
    void topluZimmetVeIadeGecersizKalemdeAtomicRollbackYapar() {
        String suffix = suffix();
        Depo depo = depo("atomic-source-" + suffix);
        Depo iadeDeposu = depo("atomic-return-" + suffix);
        Envanter assignCandidate = envanter(
            "assign-a-" + suffix, "Assign A", null, null, null, depo);
        String personelKodu = "P-ATOMIC-" + suffix;

        assertThatThrownBy(() -> personelZimmetService.topluZimmetOlustur(personelKodu,
            new TopluPersonelZimmetRequest(List.of(
                item(assignCandidate), new TopluEnvanterItemRequest("zz-missing-" + suffix, 1L)),
                null, "tester")))
            .isInstanceOf(ResourceNotFoundException.class);
        assertThat(envanterRepository.findById(assignCandidate.getGuid())
            .orElseThrow().getDepo().getGuid()).isEqualTo(depo.getGuid());
        assertThat(personelHareketRepository.findByEnvanterGuidOrderByIdDesc(
            assignCandidate.getGuid())).isEmpty();

        Envanter returnCandidate = envanter(
            "return-a-" + suffix, "Return A", null, null, null, depo);
        personelZimmetService.topluZimmetOlustur(personelKodu,
            new TopluPersonelZimmetRequest(List.of(item(returnCandidate)), null, "tester"));
        assertThatThrownBy(() -> personelZimmetService.topluIadeEt(personelKodu,
            new TopluPersonelIadeRequest(iadeDeposu.getGuid(), List.of(
                item(returnCandidate), new TopluEnvanterItemRequest("zz-missing-return-" + suffix, 1L)),
                null, "tester")))
            .isInstanceOf(ResourceNotFoundException.class);
        assertThat(envanterRepository.findById(returnCandidate.getGuid()).orElseThrow().getDepo())
            .isNull();
        assertThat(personelHareketRepository.findByEnvanterGuidOrderByIdDesc(
            returnCandidate.getGuid())).hasSize(1);
    }

    private Depo depo(String guid) {
        Depo depo = new Depo();
        depo.setGuid(guid);
        depo.setAd(guid);
        return depoRepository.saveAndFlush(depo);
    }

    private Envanter envanter(
        String guid, String ad, String seriNo, String barkod, Long demirbasNo, Depo depo
    ) {
        Envanter envanter = new Envanter();
        envanter.setGuid(guid);
        envanter.setEnvanterAdi(ad);
        envanter.setEnvanterTuru(EnvanterTuru.DEMIRBAS);
        envanter.setSeriNo(seriNo);
        envanter.setBarkod(barkod);
        envanter.setDemirbasNumarasi(demirbasNo);
        envanter.setDepo(depo);
        return envanterRepository.saveAndFlush(envanter);
    }

    private static List<TopluEnvanterItemRequest> items(Envanter... envanterler) {
        return java.util.Arrays.stream(envanterler).map(BusinessEndpointsIntegrationTest::item).toList();
    }

    private static TopluEnvanterItemRequest item(Envanter envanter) {
        return new TopluEnvanterItemRequest(envanter.getGuid(), 1L);
    }

    private static String suffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
