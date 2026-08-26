package com.stoktakip.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.stoktakip.dto.MarkaCreateRequest;
import com.stoktakip.dto.MarkaUpdateRequest;
import com.stoktakip.dto.ModelCreateRequest;
import com.stoktakip.dto.ModelUpdateRequest;
import com.stoktakip.entity.Marka;
import com.stoktakip.entity.Model;
import com.stoktakip.exception.ResourceNotFoundException;
import com.stoktakip.repository.MarkaRepository;
import com.stoktakip.repository.ModelRepository;
import com.stoktakip.service.MarkaService;
import com.stoktakip.service.ModelService;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;

@SpringBootTest
@Transactional
class MarkaModelControllerIntegrationTest {

    @Autowired MarkaService markaService;
    @Autowired ModelService modelService;
    @Autowired MarkaRepository markaRepository;
    @Autowired ModelRepository modelRepository;
    @Autowired JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        Marka samsung = marka("marka-samsung", "Samsung");
        Marka bosch = marka("marka-bosch", "Bosch");
        markaRepository.save(samsung);
        markaRepository.save(bosch);

        Model thinkPad = model("model-thinkpad", "ThinkPad");
        thinkPad.setMarka(samsung);
        modelRepository.save(thinkPad);
        Model thinkBook = model("model-thinkbook", "ThinkBook");
        thinkBook.setMarka(bosch);
        modelRepository.save(thinkBook);
        modelRepository.flush();
    }

    @Test
    void markaSearchCaseInsensitiveVePartialCalisir() {
        assertThat(markaService.searchByName("sAmS"))
            .extracting(response -> response.markaAdi())
            .containsExactly("Samsung");
    }

    @Test
    void modelSearchCaseInsensitiveVePartialListeDoner() {
        assertThat(modelService.searchByMarkaAndName("marka-samsung", "tHiNk"))
            .extracting(response -> response.modelAdi())
            .containsExactly("ThinkPad");
    }

    @Test
    void modellerMarkayaGoreFiltrelenirVeBaskaMarkaKaristirilmaz() {
        assertThat(modelService.getByMarkaGuid("marka-samsung"))
            .extracting(response -> response.modelAdi())
            .containsExactly("ThinkPad");
        assertThat(modelService.getByMarkaGuid("marka-bosch"))
            .extracting(response -> response.modelAdi())
            .containsExactly("ThinkBook");
    }

    @Test
    void markaGetAllCreateUpdateDeleteCalisir() {
        assertThat(markaService.getAll()).hasSize(2);

        var created = markaService.create(new MarkaCreateRequest("Lenovo", "Yeni"));
        assertThat(created.guid()).isNotBlank();
        assertThat(created.markaAdi()).isEqualTo("Lenovo");

        var updated = markaService.update(
            "marka-bosch", new MarkaUpdateRequest("Bosch Updated", "Güncel"));
        assertThat(updated.guid()).isEqualTo("marka-bosch");
        assertThat(updated.markaAdi()).isEqualTo("Bosch Updated");

        markaService.delete(created.guid());
        assertThat(markaRepository.findById(created.guid())).isEmpty();
        assertThat(jdbcTemplate.queryForObject(
            "select count(*) from MARKA where GUID = ? and SILINDI = true",
            Integer.class, created.guid())).isEqualTo(1);
        assertThat(markaService.getAll()).noneMatch(marka -> marka.guid().equals(created.guid()));
        assertThatThrownBy(() -> markaService.delete(created.guid()))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void modelGetAllCreateUpdateDeleteCalisirVeMarkaIliskisiKorunur() {
        assertThat(modelService.getAll()).hasSize(2);

        var created = modelService.create(new ModelCreateRequest(
            "Galaxy S", null, "marka-samsung", null));
        assertThat(created.guid()).isNotBlank();
        assertThat(created.markaGuid()).isEqualTo("marka-samsung");

        var updated = modelService.update(
            "model-thinkpad", new ModelUpdateRequest(
                "ThinkPad X1", "Güncel", "marka-bosch"));
        assertThat(updated.guid()).isEqualTo("model-thinkpad");
        assertThat(updated.markaGuid()).isEqualTo("marka-bosch");
        assertThat(updated.markaAdi()).isEqualTo("Bosch");

        modelService.delete("model-thinkpad");
        assertThat(modelRepository.findById("model-thinkpad")).isEmpty();
        assertThat(jdbcTemplate.queryForObject(
            "select count(*) from MODEL where GUID = ? and SILINDI = true",
            Integer.class, "model-thinkpad")).isEqualTo(1);
        assertThat(modelService.getAll()).noneMatch(model -> model.guid().equals("model-thinkpad"));
        assertThatThrownBy(() -> modelService.delete("model-thinkpad"))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getByGuidRouteYokPutByGuidRouteVar() {
        assertThat(hasMapping(MarkaController.class, GetMapping.class, "/{guid}"))
            .isFalse();
        assertThat(hasMapping(ModelController.class, GetMapping.class, "/{guid}"))
            .isFalse();
        assertThat(hasMapping(MarkaController.class, PutMapping.class, "/{guid}"))
            .isTrue();
        assertThat(hasMapping(ModelController.class, PutMapping.class, "/{guid}"))
            .isTrue();
    }

    private static boolean hasMapping(
        Class<?> controller,
        Class<? extends java.lang.annotation.Annotation> annotationType,
        String path
    ) {
        return Arrays.stream(controller.getDeclaredMethods())
            .anyMatch(method -> mappingPaths(method, annotationType).contains(path));
    }

    private static java.util.List<String> mappingPaths(
        Method method,
        Class<? extends java.lang.annotation.Annotation> annotationType
    ) {
        if (annotationType == GetMapping.class) {
            GetMapping mapping = method.getAnnotation(GetMapping.class);
            return mapping == null ? java.util.List.of() : Arrays.asList(mapping.value());
        }
        PutMapping mapping = method.getAnnotation(PutMapping.class);
        return mapping == null ? java.util.List.of() : Arrays.asList(mapping.value());
    }

    private static Marka marka(String guid, String ad) {
        Marka marka = new Marka();
        marka.setGuid(guid);
        marka.setMarkaAdi(ad);
        return marka;
    }

    private static Model model(String guid, String ad) {
        Model model = new Model();
        model.setGuid(guid);
        model.setModelAdi(ad);
        return model;
    }
}
