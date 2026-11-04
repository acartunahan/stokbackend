package com.stoktakip.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.stoktakip.dto.ArizaCreateRequest;
import com.stoktakip.dto.ArizaFilter;
import com.stoktakip.dto.AparatCreateRequest;
import com.stoktakip.dto.EnvanterAciklamaCreateRequest;
import com.stoktakip.dto.EnvanterCreateRequest;
import com.stoktakip.dto.EnvanterDetayCreateRequest;
import com.stoktakip.dto.EnvanterFilter;
import com.stoktakip.dto.ZimmetTalebi;
import com.stoktakip.entity.Depo;
import com.stoktakip.entity.Firma;
import com.stoktakip.entity.Ihale;
import com.stoktakip.entity.Kategori;
import com.stoktakip.entity.Marka;
import com.stoktakip.entity.Model;
import com.stoktakip.enums.EnvanterTuru;
import com.stoktakip.repository.DepoRepository;
import com.stoktakip.repository.FirmaRepository;
import com.stoktakip.repository.IhaleRepository;
import com.stoktakip.repository.KategoriRepository;
import com.stoktakip.repository.MarkaRepository;
import com.stoktakip.repository.ModelRepository;
import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;

@SpringBootTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
/** Liste uçlarının sorgu sayısı kayıt sayısıyla büyümemeli (N+1 koruması). */
class SorguSayisiTest {
    private final Map<String, Long> olcumler = new LinkedHashMap<>();

    @Autowired EntityManagerFactory emf;
    @Autowired EnvanterService envanterService;
    @Autowired ModelService modelService;
    @Autowired MarkaService markaService;
    @Autowired KategoriService kategoriService;
    @Autowired IhaleService ihaleService;
    @Autowired FirmaService firmaService;
    @Autowired DepoService depoService;
    @Autowired AparatService aparatService;
    @Autowired ArizaService arizaService;
    @Autowired HareketService hareketService;
    @Autowired ZimmetService zimmetService;
    @Autowired PersonelZimmetService personelZimmetService;
    @Autowired EnvanterAciklamaService aciklamaService;
    @Autowired SarfService sarfService;
    @Autowired SarfLogService sarfLogService;
    @Autowired RaporService raporService;
    @Autowired DepoRepository depoRepository;
    @Autowired FirmaRepository firmaRepository;
    @Autowired IhaleRepository ihaleRepository;
    @Autowired MarkaRepository markaRepository;
    @Autowired ModelRepository modelRepository;
    @Autowired KategoriRepository kategoriRepository;

    @Test
    void listeSorgulariKayitSayisiylaArtmaz() {
        Map<String, Long> ilk = null;
        for (int n : new int[] {3, 12}) {
            olcumler.clear();
            String s = UUID.randomUUID().toString().substring(0, 6);
            Depo depo = new Depo(); depo.setGuid("pd-" + s); depo.setAd("pd"); depoRepository.save(depo);
            Depo depo2 = new Depo(); depo2.setGuid("pd2-" + s); depo2.setAd("pd2"); depoRepository.save(depo2);
            Kategori ust = null;
            String envGuid = null;
            for (int i = 0; i < n; i++) {
                Kategori k = new Kategori(); k.setGuid("pk-" + s + i); k.setBaslik("k" + i); k.setParent(ust);
                kategoriRepository.save(k); ust = k;
                Marka m = new Marka(); m.setGuid("pm-" + s + i); m.setMarkaAdi("m" + i); markaRepository.save(m);
                Model mo = new Model(); mo.setGuid("pmo-" + s + i); mo.setModelAdi("mo" + i); mo.setMarka(m); mo.setKategori(k);
                modelRepository.save(mo);
                Firma f = new Firma(); f.setGuid("pf-" + s + i); f.setAd("f" + i); f.setKaynakAlani(EnvanterTuru.DEMIRBAS);
                firmaRepository.save(f);
                Ihale ih = new Ihale(); ih.setGuid("pi-" + s + i); ih.setAd("i" + i); ih.setFirma(f); ihaleRepository.save(ih);
                var e = envanterService.create(new EnvanterCreateRequest("e" + i,
                    i % 2 == 0 ? EnvanterTuru.DEMIRBAS : EnvanterTuru.SARF, depo.getGuid(), mo.getGuid(),
                    1000L + i, "sn" + s + i, "bc" + s + i, null, null, null,
                    new EnvanterDetayCreateRequest(null, null, null, ih.getGuid(), null,
                        LocalDateTime.now(), LocalDateTime.now().plusDays(10))));
                envGuid = e.guid();
                if (i % 2 == 0) {
                    zimmetService.personeleZimmetle(new ZimmetTalebi(e.guid(), "P" + s, null, null));
                    arizaService.create(e.guid(), new ArizaCreateRequest(LocalDateTime.now(), "ariza", null, null));
                    aparatService.create(new AparatCreateRequest(e.guid(), "ap", null));
                }
                aciklamaService.create(e.guid(), new EnvanterAciklamaCreateRequest("not", null, null, null));
            }
            final String eg = envGuid;
            count("envanter.getAll", () -> envanterService.getAll());
            count("envanter.search", () -> envanterService.search(new EnvanterFilter(null, null, null, depo.getGuid(), null, null, null, null, null, null), PageRequest.of(0, 50)));
            count("envanter.getDemirbaslar", () -> envanterService.getDemirbaslar());
            count("envanter.getByDepo", () -> envanterService.getByDepo(depo.getGuid(), null, PageRequest.of(0, 50)));
            count("model.getAll", () -> modelService.getAll());
            count("marka.getAll", () -> markaService.getAll());
            count("kategori.getAll", () -> kategoriService.getAll());
            count("kategori.tree", () -> kategoriService.getCategoryTree());
            count("ihale.getAll", () -> ihaleService.getAll());
            count("firma.getAll", () -> firmaService.getAll());
            count("depo.getAll", () -> depoService.getAll());
            count("aparat.getAll", () -> aparatService.getAll());
            count("ariza.search", () -> arizaService.search(new ArizaFilter(null, null, null, null, null, null), PageRequest.of(0, 50)));
            count("hareket.depo", () -> hareketService.getDepoHareketleri(depo.getGuid(), null, null, PageRequest.of(0, 50)));
            count("personel.envanterleri", () -> personelZimmetService.getPersonelEnvanterleri("P" + s));
            count("personel.gecmis", () -> personelZimmetService.getZimmetGecmisi("P" + s));
            count("zimmet.personelGecmisi", () -> zimmetService.personelGecmisi(eg));
            count("sarf.stok", () -> sarfService.getStok(null, null));
            count("rapor.ozet", () -> raporService.getOzet());
            count("rapor.garanti", () -> raporService.getGarantisiBitecekler(30));
            if (ilk == null) {
                ilk = new LinkedHashMap<>(olcumler);
            } else {
                assertThat(olcumler).isEqualTo(ilk);
            }
        }
    }

    private void count(String name, Supplier<Object> call) {
        Statistics stats = emf.unwrap(SessionFactory.class).getStatistics();
        stats.clear();
        call.get();
        olcumler.put(name, stats.getPrepareStatementCount());
    }
}
