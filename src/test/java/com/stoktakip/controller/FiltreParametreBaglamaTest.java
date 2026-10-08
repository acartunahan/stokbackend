package com.stoktakip.controller;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stoktakip.entity.Depo;
import com.stoktakip.entity.Envanter;
import com.stoktakip.entity.Marka;
import com.stoktakip.entity.Model;
import com.stoktakip.enums.EnvanterTuru;
import com.stoktakip.repository.DepoRepository;
import com.stoktakip.repository.EnvanterRepository;
import com.stoktakip.repository.MarkaRepository;
import com.stoktakip.repository.ModelRepository;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/**
 * HTTP parametre bağlamasının kanıtı: virgüllü tek parametre ve tekrarlı parametre
 * aynı listeye bağlanır, eski tek değerli çağrı aynı sonucu verir, geçersiz enum 400,
 * 100 değer sınırı 422 döner.
 */
@SpringBootTest
@Transactional
class FiltreParametreBaglamaTest {

    @Autowired WebApplicationContext context;
    @Autowired EnvanterRepository envanterRepository;
    @Autowired DepoRepository depoRepository;
    @Autowired MarkaRepository markaRepository;
    @Autowired ModelRepository modelRepository;

    MockMvc mvc;
    String s;
    String markaA;
    String markaB;
    String depo1;
    String depo2;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        s = UUID.randomUUID().toString().substring(0, 8);
        Marka a = marka("A");
        Marka b = marka("B");
        markaA = a.getGuid();
        markaB = b.getGuid();
        Model modelA = model("A", a);
        Model modelB = model("B", b);
        Depo d1 = depo("1");
        Depo d2 = depo("2");
        depo1 = d1.getGuid();
        depo2 = d2.getGuid();
        envanter("e1", modelA, EnvanterTuru.DEMIRBAS, d1);
        envanter("e2", modelA, EnvanterTuru.SARF, d2);
        envanter("e3", modelB, EnvanterTuru.DEMIRBAS, d1);
    }

    @Test
    void virgulluVeTekrarliParametreAyniListeyeBaglanir() throws Exception {
        mvc.perform(get("/api/envanterler").param("markaGuid", markaA + "," + markaB))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(3))
            .andExpect(jsonPath("$.content[*].guid").value(
                containsInAnyOrder(id("e1"), id("e2"), id("e3"))));
        mvc.perform(get("/api/envanterler").param("markaGuid", markaA, markaB))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    void tekDegerliEskiCagriAyniKalir() throws Exception {
        mvc.perform(get("/api/envanterler").param("markaGuid", markaA))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(2))
            .andExpect(jsonPath("$.content[*].guid").value(
                containsInAnyOrder(id("e1"), id("e2"))));
        mvc.perform(get("/api/envanterler").param("envanterTuru", "SARF").param("q", s))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void bosParametreSuzgecUygulamaz() throws Exception {
        mvc.perform(get("/api/envanterler").param("q", s).param("markaGuid", ""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(3));
        mvc.perform(get("/api/envanterler").param("q", s).param("markaGuid", ",,"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(3));
        mvc.perform(get("/api/envanterler").param("q", s).param("markaGuid", markaB + ", ,"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void enumListesiVirgulleBaglanirGecersizUye400() throws Exception {
        mvc.perform(get("/api/envanterler").param("q", s).param("envanterTuru", "DEMIRBAS,SARF"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(3));
        mvc.perform(get("/api/envanterler").param("envanterTuru", "DEMIRBAS,YOK"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("envanterTuru: geçersiz değer"));
    }

    @Test
    void yuzDegerSiniriAsilinca422() throws Exception {
        String yuz = IntStream.range(0, 100).mapToObj(i -> "g" + i)
            .collect(Collectors.joining(","));
        mvc.perform(get("/api/envanterler").param("markaGuid", yuz))
            .andExpect(status().isOk());
        mvc.perform(get("/api/envanterler").param("markaGuid", yuz + ",g100"))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.message")
                .value("markaGuid için en fazla 100 değer gönderilebilir"));
    }

    @Test
    void depoUcuCokluDegerAlirVeYalnizKendiDeposunuDoner() throws Exception {
        mvc.perform(get("/api/depolar/{guid}/envanterler", depo1)
                .param("markaGuid", markaA + "," + markaB)
                .param("depoGuid", depo2))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(2))
            .andExpect(jsonPath("$.content[*].guid").value(
                containsInAnyOrder(id("e1"), id("e3"))));
        mvc.perform(get("/api/depolar/{guid}/envanterler", depo1)
                .param("envanterTuru", "SARF,DEMIRBAS"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(2));
        mvc.perform(get("/api/depolar/{guid}/envanterler", depo1)
                .param("envanterTuru", "SARF,YOK"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("envanterTuru: geçersiz değer"));
    }

    private String id(String ad) {
        return "baglama-" + ad + "-" + s;
    }

    private Marka marka(String ad) {
        Marka m = new Marka();
        m.setGuid("baglama-marka-" + ad + "-" + s);
        m.setMarkaAdi("Marka " + ad + " " + s);
        return markaRepository.saveAndFlush(m);
    }

    private Model model(String ad, Marka marka) {
        Model m = new Model();
        m.setGuid("baglama-model-" + ad + "-" + s);
        m.setModelAdi("Model " + ad + " " + s);
        m.setMarka(marka);
        return modelRepository.saveAndFlush(m);
    }

    private Depo depo(String ad) {
        Depo d = new Depo();
        d.setGuid("baglama-depo-" + ad + "-" + s);
        d.setAd("Depo " + ad + " " + s);
        return depoRepository.saveAndFlush(d);
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
}
