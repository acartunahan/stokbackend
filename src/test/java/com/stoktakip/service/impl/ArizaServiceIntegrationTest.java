package com.stoktakip.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.stoktakip.dto.ArizaCreateRequest;
import com.stoktakip.dto.ArizaDurumuCreateRequest;
import com.stoktakip.dto.ArizaFilter;
import com.stoktakip.dto.ArizaKapatRequest;
import com.stoktakip.entity.Depo;
import com.stoktakip.entity.DepoZimmetHareket;
import com.stoktakip.entity.Envanter;
import com.stoktakip.entity.PersonelZimmetHareket;
import com.stoktakip.enums.ArizaDurumTipi;
import com.stoktakip.enums.EnvanterSahiplikTipi;
import com.stoktakip.enums.EnvanterTuru;
import com.stoktakip.enums.ZimmetHareketTipi;
import com.stoktakip.exception.BusinessRuleException;
import com.stoktakip.exception.ResourceNotFoundException;
import com.stoktakip.repository.ArizaDurumuRepository;
import com.stoktakip.repository.ArizaRepository;
import com.stoktakip.repository.DepoRepository;
import com.stoktakip.repository.DepoZimmetHareketRepository;
import com.stoktakip.repository.EnvanterRepository;
import com.stoktakip.repository.PersonelZimmetHareketRepository;
import com.stoktakip.service.ArizaService;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@TestPropertySource(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
class ArizaServiceIntegrationTest {
    private static final LocalDateTime T2015 = LocalDateTime.of(2015, 1, 1, 9, 0);
    private static final LocalDateTime T2016 = LocalDateTime.of(2016, 6, 10, 10, 0);
    private static final LocalDateTime T2017 = LocalDateTime.of(2017, 12, 31, 18, 0);
    private static final LocalDateTime T2018 = LocalDateTime.of(2018, 1, 1, 9, 0);
    private static final LocalDateTime T2019 = LocalDateTime.of(2019, 3, 15, 11, 0);
    private static final LocalDateTime T2020 = LocalDateTime.of(2020, 12, 31, 18, 0);

    @Autowired ArizaService service;
    @Autowired ArizaRepository arizaRepository;
    @Autowired ArizaDurumuRepository durumRepository;
    @Autowired EnvanterRepository envanterRepository;
    @Autowired DepoRepository depoRepository;
    @Autowired PersonelZimmetHareketRepository personelRepository;
    @Autowired DepoZimmetHareketRepository depoHareketRepository;
    @Autowired EntityManager entityManager;

    @Test
    void envanterIcinArizaOlusturulurVeIlkHistoryYazilir() {
        Envanter e = inventory("create");
        var result = create(e, T2019);
        assertThat(result.sonDurum()).isEqualTo(ArizaDurumTipi.ARIZA_BILDIRILDI);
        assertThat(durumRepository.findHistory(result.guid()))
            .extracting(d -> d.getDurumTipi()).containsExactly(ArizaDurumTipi.ARIZA_BILDIRILDI);
    }

    @Test
    void varOlmayanEnvantereArizaOlusturulamaz() {
        assertThatThrownBy(() -> service.create("missing", request(T2019)))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void ayniEnvanterdeIkinciAcikArizaEngellenir() {
        Envanter e = inventory("open");
        create(e, T2019);
        assertThatThrownBy(() -> create(e, T2020)).isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("zaten açık");
    }

    @Test
    void farkliYillardakiArizalarDogruPersoneliBulur() {
        Envanter e = inventory("timeline");
        person(e, "X", ZimmetHareketTipi.PERSONELE_ZIMMET, T2015);
        person(e, "X", ZimmetHareketTipi.IADE, T2017);
        person(e, "Y", ZimmetHareketTipi.PERSONELE_ZIMMET, T2018);
        person(e, "Y", ZimmetHareketTipi.IADE, T2020);

        var first = create(e, T2016);
        assertThat(first.arizaAnindakiSahiplik().personelKodu()).isEqualTo("X");
        service.close(first.guid(), new ArizaKapatRequest(null));
        var second = create(e, T2019);
        assertThat(second.arizaAnindakiSahiplik().personelKodu()).isEqualTo("Y");
    }

    @Test
    void arizaAnindaDepodaysaDepoBulunur() {
        Envanter e = inventory("depot");
        Depo d = depot("historical");
        depotMovement(e, d, T2018);
        var owner = create(e, T2019).arizaAnindakiSahiplik();
        assertThat(owner.tip()).isEqualTo(EnvanterSahiplikTipi.DEPO);
        assertThat(owner.depoGuid()).isEqualTo(d.getGuid());
    }

    @Test
    void tarihselSahiplikYoksaBelirsizDoner() {
        var owner = create(inventory("unknown"), T2019).arizaAnindakiSahiplik();
        assertThat(owner.tip()).isEqualTo(EnvanterSahiplikTipi.BELIRSIZ);
    }

    @Test
    void cakisanPersonelZimmetleriVeriButunluguSorunuOlur() {
        Envanter e = inventory("overlap");
        person(e, "X", ZimmetHareketTipi.PERSONELE_ZIMMET, T2015);
        person(e, "Y", ZimmetHareketTipi.PERSONELE_ZIMMET, T2018);
        assertThatThrownBy(() -> create(e, T2019)).isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("birden fazla aktif personel");
    }

    @Test
    void durumlarKronolojikGelirVeSonDurumGuncellenir() {
        var fault = create(inventory("status"), T2019);
        service.addStatus(fault.guid(), status(ArizaDurumTipi.ARIZA_TESPITI_YAPILIYOR, T2019.plusHours(1)));
        service.addStatus(fault.guid(), status(ArizaDurumTipi.TAMIRDE, T2019.plusHours(2)));
        assertThat(service.getStatuses(fault.guid())).extracting(s -> s.durumTipi())
            .containsExactly(ArizaDurumTipi.ARIZA_BILDIRILDI,
                ArizaDurumTipi.ARIZA_TESPITI_YAPILIYOR, ArizaDurumTipi.TAMIRDE);
        assertThat(arizaRepository.findById(fault.guid()).orElseThrow().getSonDurum())
            .isEqualTo(ArizaDurumTipi.TAMIRDE);
    }

    @Test
    void gecersizDurumGecisiEngellenir() {
        var fault = create(inventory("invalid-transition"), T2019);
        assertThatThrownBy(() -> service.addStatus(fault.guid(),
            status(ArizaDurumTipi.TAMIR_EDILDI, T2019.plusHours(1))))
            .isInstanceOf(BusinessRuleException.class).hasMessageContaining("Geçersiz");
    }

    @Test
    void oncekiDurumdanEskiTarihliDurumEngellenir() {
        var fault = create(inventory("old-status"), T2019);
        assertThatThrownBy(() -> service.addStatus(fault.guid(),
            status(ArizaDurumTipi.ARIZA_TESPITI_YAPILIYOR, T2019.minusSeconds(1))))
            .isInstanceOf(BusinessRuleException.class).hasMessageContaining("önce");
    }

    @Test
    void kapatmaArizayiVeHistoryyiAyniAndaGunceller() {
        var fault = create(inventory("close"), T2019);
        var closed = service.close(fault.guid(), new ArizaKapatRequest("Tamamlandı"));
        assertThat(closed.kapandiMi()).isTrue();
        assertThat(closed.sonDurum()).isEqualTo(ArizaDurumTipi.KAPATILDI);
        assertThat(service.getStatuses(fault.guid()).getLast().durumTipi())
            .isEqualTo(ArizaDurumTipi.KAPATILDI);
    }

    @Test
    void kapanmisArizayaDurumEklenemez() {
        var fault = create(inventory("closed-status"), T2019);
        service.close(fault.guid(), new ArizaKapatRequest(null));
        assertThatThrownBy(() -> service.addStatus(fault.guid(),
            status(ArizaDurumTipi.ARIZA_TESPITI_YAPILIYOR, LocalDateTime.now())))
            .isInstanceOf(BusinessRuleException.class).hasMessageContaining("Kapanmış");
    }

    @Test
    void kapatildidanSonraYeniArizaAcilabilir() {
        Envanter e = inventory("reopen");
        var first = create(e, T2019);
        service.close(first.guid(), new ArizaKapatRequest(null));
        assertThat(create(e, T2020).guid()).isNotEqualTo(first.guid());
    }

    @Test
    void paginationCalisir() {
        create(inventory("page-1"), T2019);
        create(inventory("page-2"), T2019);
        create(inventory("page-3"), T2019);
        var page = service.search(new ArizaFilter(null, null, null, null, null, null),
            PageRequest.of(0, 2));
        assertThat(page.content()).hasSize(2);
        assertThat(page.totalElements()).isGreaterThanOrEqualTo(3);
    }

    @Test
    void listeSahipliginiNArtıBirOlmadanTopluCozuyor() {
        for (int i = 0; i < 4; i++) {
            Envanter e = inventory("batch-" + i);
            person(e, "P-" + i, ZimmetHareketTipi.PERSONELE_ZIMMET, T2018);
            create(e, T2019);
        }
        entityManager.flush();
        entityManager.clear();
        var statistics = entityManager.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
        statistics.clear();
        var page = service.search(new ArizaFilter(null, null, null, null, null, null), PageRequest.of(0, 20));
        assertThat(page.content()).hasSizeGreaterThanOrEqualTo(4);
        assertThat(statistics.getPrepareStatementCount()).isLessThanOrEqualTo(4);
    }

    @Test
    void sonDurumBirdenCokDegerleSuzulur() {
        // Yalnız bu testin arızalarını kapsayan dar tarih penceresi.
        LocalDateTime an = LocalDateTime.of(2003, 3, 3, 3, 3);
        String k = java.util.UUID.randomUUID().toString().substring(0, 8);
        create(inventory("durum-1-" + k), an);
        var tamirde = create(inventory("durum-2-" + k), an);
        var kapali = create(inventory("durum-3-" + k), an);
        setStatus(tamirde.guid(), ArizaDurumTipi.TAMIRDE, false);
        setStatus(kapali.guid(), ArizaDurumTipi.KAPATILDI, true);

        assertThat(search(an, List.of(ArizaDurumTipi.TAMIRDE), null)).isEqualTo(1);
        assertThat(search(an, List.of(ArizaDurumTipi.ARIZA_BILDIRILDI, ArizaDurumTipi.TAMIRDE),
            null)).isEqualTo(2);
        assertThat(search(an, List.of(), null)).isEqualTo(3);
        assertThat(search(an, null, null)).isEqualTo(3);
        assertThat(search(an, java.util.Arrays.asList(ArizaDurumTipi.TAMIRDE, null,
            ArizaDurumTipi.TAMIRDE), null)).isEqualTo(1);
        // Alanlar arası VE: tamirdeki arıza kapalı değildir.
        assertThat(search(an, List.of(ArizaDurumTipi.TAMIRDE), true)).isZero();
        assertThat(search(an, List.of(ArizaDurumTipi.KAPATILDI, ArizaDurumTipi.TAMIRDE), true))
            .isEqualTo(1);
    }

    @Test
    void aramadaYuzdeAltCizgiVeTersBoluDuzMetinSayilir() {
        LocalDateTime an = LocalDateTime.of(2004, 4, 4, 4, 4);
        String k = java.util.UUID.randomUUID().toString().substring(0, 8);
        fault(inventory("kacis-1-" + k), an, "%100 arıza");
        fault(inventory("kacis-2-" + k), an, "A_B kartı");
        fault(inventory("kacis-3-" + k), an, "AB kartı");
        fault(inventory("kacis-4-" + k), an, "C:\\yol bozuk");
        fault(inventory("kacis-5-" + k), an, "Işık yanmıyor");

        assertThat(searchText(an, "%100")).isEqualTo(1);
        assertThat(searchText(an, "%")).isEqualTo(1);
        assertThat(searchText(an, "A_B")).isEqualTo(1);
        assertThat(searchText(an, "_")).isEqualTo(1);
        assertThat(searchText(an, "\\")).isEqualTo(1);
        assertThat(searchText(an, "ab kart")).isEqualTo(1);
        assertThat(searchText(an, "kartı")).isEqualTo(2);
        assertThat(searchText(an, "  kartı  ")).isEqualTo(2);
    }

    @Test
    void aramaJvmYerelindenBagimsizKucukHarfeCevrilir() {
        LocalDateTime an = LocalDateTime.of(2005, 5, 5, 5, 5);
        String k = java.util.UUID.randomUUID().toString().substring(0, 8);
        fault(inventory("yerel-1-" + k), an, "Işık yanmıyor");
        fault(inventory("yerel-2-" + k), an, "KIRIK EKRAN");
        java.util.Locale onceki = java.util.Locale.getDefault();
        try {
            // Türkçe yerelde "I".toLowerCase() "ı" verir. Arama iki tarafı da veritabanı
            // LOWER'ıyla çevirir; yazılan metnin aynısı her yerelde bulunur. Not: H2'nin
            // LOWER'ı JVM yerelini kullanır (ölçüldü), Oracle'ınki NLS ayarını; eski
            // JVM toLowerCase() sürümüyle farkı yalnız Oracle'da ortaya çıkar.
            java.util.Locale.setDefault(java.util.Locale.forLanguageTag("tr-TR"));
            assertThat(searchText(an, "Işık yanmıyor")).isEqualTo(1);
            assertThat(searchText(an, "KIRIK")).isEqualTo(1);
        } finally {
            java.util.Locale.setDefault(onceki);
        }
    }

    private void fault(Envanter e, LocalDateTime an, String aciklama) {
        service.create(e.getGuid(), new ArizaCreateRequest(an, aciklama, null, null));
    }

    private long searchText(LocalDateTime an, String q) {
        return service.search(new ArizaFilter(null, null, null, an, an, q),
            PageRequest.of(0, 50)).totalElements();
    }

    private long search(LocalDateTime an, List<ArizaDurumTipi> durumlar, Boolean kapandiMi) {
        return service.search(new ArizaFilter(null, durumlar, kapandiMi, an, an, null),
            PageRequest.of(0, 50)).totalElements();
    }

    private void setStatus(String arizaGuid, ArizaDurumTipi durum, boolean kapandi) {
        var ariza = arizaRepository.findById(arizaGuid).orElseThrow();
        ariza.setSonDurum(durum);
        ariza.setKapandiMi(kapandi);
        arizaRepository.saveAndFlush(ariza);
    }

    private Envanter inventory(String suffix) {
        Envanter e = new Envanter();
        e.setGuid("fault-inventory-" + suffix);
        e.setEnvanterAdi("Cihaz " + suffix);
        e.setEnvanterTuru(EnvanterTuru.DEMIRBAS);
        return envanterRepository.saveAndFlush(e);
    }

    private Depo depot(String suffix) {
        Depo d = new Depo();
        d.setGuid("fault-depot-" + suffix);
        d.setAd("Depo " + suffix);
        return depoRepository.saveAndFlush(d);
    }

    private void person(Envanter e, String person, ZimmetHareketTipi type, LocalDateTime date) {
        PersonelZimmetHareket h = new PersonelZimmetHareket();
        h.setEnvanter(e);
        h.setPersonelKodu(person);
        h.setHareketTipi(type);
        h.setZimmetTuru("DEMIRBAS");
        h.setAktif(type == ZimmetHareketTipi.PERSONELE_ZIMMET);
        h.setOlusturmaTarihi(date);
        personelRepository.saveAndFlush(h);
    }

    private void depotMovement(Envanter e, Depo d, LocalDateTime date) {
        DepoZimmetHareket h = new DepoZimmetHareket();
        h.setEnvanter(e);
        h.setHareketTipi(ZimmetHareketTipi.DEPO_TRANSFERI);
        h.setZimmetTuru("DEMIRBAS");
        h.setAdet(1L);
        h.setGidecegiDepo(d);
        h.setOlusturmaTarihi(date);
        depoHareketRepository.saveAndFlush(h);
    }

    private com.stoktakip.dto.ArizaResponse create(Envanter e, LocalDateTime date) {
        return service.create(e.getGuid(), request(date));
    }

    private static ArizaCreateRequest request(LocalDateTime date) {
        return new ArizaCreateRequest(date, "Cihaz açılmıyor", "B-1", null);
    }

    private static ArizaDurumuCreateRequest status(ArizaDurumTipi type, LocalDateTime date) {
        return new ArizaDurumuCreateRequest(type, date, null, null, null, null, null,
            null, null, null, null);
    }
}
