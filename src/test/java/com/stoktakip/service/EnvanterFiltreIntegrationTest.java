package com.stoktakip.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.stoktakip.dto.EnvanterFilter;
import com.stoktakip.dto.EnvanterResponse;
import com.stoktakip.entity.Depo;
import com.stoktakip.entity.Envanter;
import com.stoktakip.entity.EnvanterDetay;
import com.stoktakip.entity.Firma;
import com.stoktakip.entity.Kategori;
import com.stoktakip.entity.Marka;
import com.stoktakip.entity.Model;
import com.stoktakip.enums.EnvanterTuru;
import com.stoktakip.exception.BusinessRuleException;
import com.stoktakip.repository.DepoRepository;
import com.stoktakip.repository.EnvanterDetayRepository;
import com.stoktakip.repository.EnvanterRepository;
import com.stoktakip.repository.FirmaRepository;
import com.stoktakip.repository.KategoriRepository;
import com.stoktakip.repository.MarkaRepository;
import com.stoktakip.repository.ModelRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Envanter süzgeçlerinin çoklu değer davranışı (H2). Veri her testte benzersiz
 * son ekle kurulur; diğer testlerin kalıcı verisi sonuçlara karışmaz.
 */
@SpringBootTest
@Transactional
class EnvanterFiltreIntegrationTest {

    @Autowired EnvanterService envanterService;
    @Autowired EnvanterRepository envanterRepository;
    @Autowired EnvanterDetayRepository detayRepository;
    @Autowired DepoRepository depoRepository;
    @Autowired MarkaRepository markaRepository;
    @Autowired ModelRepository modelRepository;
    @Autowired KategoriRepository kategoriRepository;
    @Autowired FirmaRepository firmaRepository;

    String s;
    Marka markaA;
    Marka markaB;
    Model modelA;
    Model modelB;
    Kategori katX;
    Kategori katY;
    Depo depo1;
    Depo depo2;
    Firma firma;

    @BeforeEach
    void setUp() {
        s = UUID.randomUUID().toString().substring(0, 8);
        markaA = marka("A");
        markaB = marka("B");
        katX = kategori("X");
        katY = kategori("Y");
        modelA = model("A", markaA, katX);
        modelB = model("B", markaB, katY);
        depo1 = depo("1");
        depo2 = depo("2");
        firma = firma("F");
        // e1: A modeli, demirbaş, depo1, firmalı detay
        // e2: A modeli, sarf, depo2
        // e3: B modeli, demirbaş, depo1
        // e4: modelsiz, demirbaş, depo1
        Envanter e1 = envanter("e1", modelA, EnvanterTuru.DEMIRBAS, depo1);
        envanter("e2", modelA, EnvanterTuru.SARF, depo2);
        envanter("e3", modelB, EnvanterTuru.DEMIRBAS, depo1);
        envanter("e4", null, EnvanterTuru.DEMIRBAS, depo1);
        detay(e1, firma);
    }

    @Test
    void tekDegerEskiCagriIleAyniSonucuVerir() {
        assertThat(guids(filter().marka(markaA.getGuid()).build())).containsExactlyInAnyOrder(
            id("e1"), id("e2"));
    }

    @Test
    void birdenCokDegerBirlesimDonerVeYinelemeYapmaz() {
        var page = envanterService.search(
            filter().marka(markaA.getGuid(), markaB.getGuid()).build(), PageRequest.of(0, 50));
        assertThat(page.totalElements()).isEqualTo(3);
        assertThat(page.content()).extracting(EnvanterResponse::guid)
            .containsExactlyInAnyOrder(id("e1"), id("e2"), id("e3"));
    }

    @Test
    void degerlerKirpilirVeBosOgelerAtilir() {
        assertThat(guids(filter().marka(" " + markaA.getGuid() + " ", "", null).build()))
            .containsExactlyInAnyOrder(id("e1"), id("e2"));
    }

    @Test
    void bosVeyaNullListeSuzgecUygulamaz() {
        assertThat(guids(filter().q(s).build())).hasSize(4);
        assertThat(guids(filter().q(s).marka().build())).hasSize(4);
        assertThat(guids(filter().q(s).marka("", "  ").build())).hasSize(4);
        assertThat(guids(filter().q(s).tur().build())).hasSize(4);
    }

    @Test
    void envanterTuruCokluDegerAlir() {
        assertThat(guids(filter().q(s).tur(EnvanterTuru.DEMIRBAS, EnvanterTuru.SARF).build()))
            .hasSize(4);
        assertThat(guids(filter().q(s).tur(EnvanterTuru.SARF).build()))
            .containsExactly(id("e2"));
    }

    @Test
    void kategoriModelDepoVeFirmaCokluDegerAlir() {
        assertThat(guids(filter().kategori(katX.getGuid(), katY.getGuid()).build()))
            .containsExactlyInAnyOrder(id("e1"), id("e2"), id("e3"));
        assertThat(guids(filter().model(modelB.getGuid(), "yok-" + s).build()))
            .containsExactly(id("e3"));
        assertThat(guids(filter().q(s).depo(depo2.getGuid()).build()))
            .containsExactly(id("e2"));
        assertThat(guids(filter().firma(firma.getGuid(), "yok-" + s).build()))
            .containsExactly(id("e1"));
    }

    @Test
    void farkliAlanlarArasindaVeUygulanir() {
        assertThat(guids(filter().marka(markaA.getGuid()).model(modelB.getGuid()).build()))
            .isEmpty();
        assertThat(guids(filter().marka(markaA.getGuid(), markaB.getGuid())
            .tur(EnvanterTuru.DEMIRBAS).depo(depo1.getGuid()).build()))
            .containsExactlyInAnyOrder(id("e1"), id("e3"));
    }

    @Test
    void depoUcundaIstemcininDepoDegeriYokSayilir() {
        var filtre = filter().marka(markaA.getGuid()).depo(depo2.getGuid()).build();
        assertThat(envanterService.getByDepo(depo1.getGuid(), filtre, PageRequest.of(0, 50))
            .content()).extracting(EnvanterResponse::guid).containsExactly(id("e1"));
    }

    @Test
    void parametreBasinaYuzDegerKabulEdilirFazlasiReddedilir() {
        List<String> yuz = new ArrayList<>(IntStream.range(0, 99)
            .mapToObj(i -> "yok-" + i).toList());
        yuz.add(markaA.getGuid());
        assertThat(guids(filter().marka(yuz.toArray(String[]::new)).build()))
            .containsExactlyInAnyOrder(id("e1"), id("e2"));

        // Yinelenen değer sınıra sayılmaz.
        List<String> yinelenen = new ArrayList<>(yuz);
        yinelenen.add(markaA.getGuid());
        assertThat(guids(filter().marka(yinelenen.toArray(String[]::new)).build())).hasSize(2);

        List<String> yuzBir = new ArrayList<>(yuz);
        yuzBir.add("fazla");
        assertThatThrownBy(() -> filter().marka(yuzBir.toArray(String[]::new)).build())
            .isInstanceOf(BusinessRuleException.class)
            .hasMessage("markaGuid için en fazla 100 değer gönderilebilir");
        assertThatThrownBy(() -> filter().kategori(yuzBir.toArray(String[]::new)).build())
            .isInstanceOf(BusinessRuleException.class)
            .hasMessage("kategoriGuid için en fazla 100 değer gönderilebilir");
    }

    @Test
    void garantiBitisAraligiSinirlarDahilSuzer() {
        String q = "Garanti-" + s;
        Envanter g1 = garantiEnvanteri("g1", q);
        Envanter g2 = garantiEnvanteri("g2", q);
        garantiEnvanteri("g3", q); // detaysız
        Envanter g4 = garantiEnvanteri("g4", q);
        garantiDetayi(g1, LocalDateTime.of(2026, 1, 10, 0, 0));
        garantiDetayi(g2, LocalDateTime.of(2026, 6, 30, 0, 0));
        garantiDetayi(g4, null); // detaylı ama garantisiz

        assertThat(guids(filter().q(q).build())).hasSize(4);
        assertThat(guids(filter().q(q).garanti(LocalDateTime.of(2026, 1, 1, 0, 0), null).build()))
            .containsExactlyInAnyOrder(id("g1"), id("g2"));
        assertThat(guids(filter().q(q).garanti(null, LocalDateTime.of(2026, 3, 1, 23, 59, 59))
            .build())).containsExactly(id("g1"));
        assertThat(guids(filter().q(q).garanti(LocalDateTime.of(2026, 1, 1, 0, 0),
            LocalDateTime.of(2026, 3, 1, 23, 59, 59)).build())).containsExactly(id("g1"));
        // Sınırlar dahil: bitiş tam 2026-06-30T00:00 iken o andaki garanti dahil.
        assertThat(guids(filter().q(q).garanti(null, LocalDateTime.of(2026, 6, 30, 0, 0))
            .build())).containsExactlyInAnyOrder(id("g1"), id("g2"));
        assertThat(guids(filter().q(q).garanti(LocalDateTime.of(2026, 6, 30, 0, 0), null)
            .build())).containsExactly(id("g2"));
        assertThat(guids(filter().q(q).garanti(LocalDateTime.of(2026, 1, 10, 0, 0),
            LocalDateTime.of(2026, 1, 10, 0, 0)).build())).containsExactly(id("g1"));
        // Başlangıç bitişten sonra: hata değil, boş sonuç.
        assertThat(guids(filter().q(q).garanti(LocalDateTime.of(2026, 7, 1, 0, 0),
            LocalDateTime.of(2026, 1, 1, 0, 0)).build())).isEmpty();
        // Garanti süzgeci firma süzgeciyle aynı detay join'ini paylaşır.
        assertThat(guids(filter().q(s).firma(firma.getGuid())
            .garanti(LocalDateTime.of(2000, 1, 1, 0, 0), null).build())).isEmpty();
    }

    @Test
    void aramaMetnindekiJokerlerDuzKarakterSayilir() {
        garantiEnvanteri("j1", "50%_indirim-" + s);
        garantiEnvanteri("j2", "50xyindirim-" + s);
        assertThat(guids(filter().q("%_indirim-" + s).build())).containsExactly(id("j1"));
        assertThat(guids(filter().q("indirim-" + s).build())).hasSize(2);
    }

    private Envanter garantiEnvanteri(String ad, String q) {
        Envanter e = new Envanter();
        e.setGuid(id(ad));
        e.setEnvanterAdi(q + " " + ad);
        e.setEnvanterTuru(EnvanterTuru.DEMIRBAS);
        return envanterRepository.saveAndFlush(e);
    }

    private void garantiDetayi(Envanter envanter, LocalDateTime bitis) {
        EnvanterDetay d = new EnvanterDetay();
        d.setGuid("filtre-detay-" + envanter.getGuid());
        d.setEnvanter(envanter);
        d.setGarantiBitisTarihi(bitis);
        detayRepository.saveAndFlush(d);
    }

    private List<String> guids(EnvanterFilter filter) {
        return envanterService.search(filter, PageRequest.of(0, 200)).content().stream()
            .map(EnvanterResponse::guid).toList();
    }

    private String id(String ad) {
        return "filtre-" + ad + "-" + s;
    }

    private static Builder filter() {
        return new Builder();
    }

    /** Konumsal record kurucusunu testte okunur tutar. */
    static final class Builder {
        List<String> kategori;
        List<String> marka;
        List<String> model;
        List<String> depo;
        List<String> firma;
        List<EnvanterTuru> tur;
        String q;
        LocalDateTime garantiBas;
        LocalDateTime garantiBit;

        Builder kategori(String... v) { kategori = Arrays.asList(v); return this; }
        Builder marka(String... v) { marka = Arrays.asList(v); return this; }
        Builder model(String... v) { model = Arrays.asList(v); return this; }
        Builder depo(String... v) { depo = Arrays.asList(v); return this; }
        Builder firma(String... v) { firma = Arrays.asList(v); return this; }
        Builder tur(EnvanterTuru... v) { tur = Arrays.asList(v); return this; }
        Builder q(String v) { q = v; return this; }
        Builder garanti(LocalDateTime bas, LocalDateTime bit) {
            garantiBas = bas;
            garantiBit = bit;
            return this;
        }

        EnvanterFilter build() {
            return new EnvanterFilter(kategori, marka, model, depo, firma, tur,
                null, null, null, q, garantiBas, garantiBit);
        }
    }

    private Marka marka(String ad) {
        Marka m = new Marka();
        m.setGuid("filtre-marka-" + ad + "-" + s);
        m.setMarkaAdi("Marka " + ad + " " + s);
        return markaRepository.saveAndFlush(m);
    }

    private Kategori kategori(String ad) {
        Kategori k = new Kategori();
        k.setGuid("filtre-kat-" + ad + "-" + s);
        k.setBaslik("Kategori " + ad + " " + s);
        return kategoriRepository.saveAndFlush(k);
    }

    private Model model(String ad, Marka marka, Kategori kategori) {
        Model m = new Model();
        m.setGuid("filtre-model-" + ad + "-" + s);
        m.setModelAdi("Model " + ad + " " + s);
        m.setMarka(marka);
        m.setKategori(kategori);
        return modelRepository.saveAndFlush(m);
    }

    private Depo depo(String ad) {
        Depo d = new Depo();
        d.setGuid("filtre-depo-" + ad + "-" + s);
        d.setAd("Depo " + ad + " " + s);
        return depoRepository.saveAndFlush(d);
    }

    private Firma firma(String ad) {
        Firma f = new Firma();
        f.setGuid("filtre-firma-" + ad + "-" + s);
        f.setAd("Firma " + ad + " " + s);
        f.setKaynakAlani(EnvanterTuru.DEMIRBAS);
        return firmaRepository.saveAndFlush(f);
    }

    private Envanter envanter(String ad, Model model, EnvanterTuru tur, Depo depo) {
        Envanter e = new Envanter();
        e.setGuid(id(ad));
        e.setEnvanterAdi("Cihaz " + ad + " " + s);
        e.setEnvanterTuru(tur);
        e.setModel(model);
        e.setDepo(depo);
        return envanterRepository.saveAndFlush(e);
    }

    private EnvanterDetay detay(Envanter envanter, Firma firma) {
        EnvanterDetay d = new EnvanterDetay();
        d.setGuid("filtre-detay-" + envanter.getGuid());
        d.setEnvanter(envanter);
        d.setFirma(firma);
        return detayRepository.saveAndFlush(d);
    }
}
