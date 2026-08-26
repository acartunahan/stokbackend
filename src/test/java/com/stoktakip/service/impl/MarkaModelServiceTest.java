package com.stoktakip.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stoktakip.dto.MarkaCreateRequest;
import com.stoktakip.dto.MarkaUpdateRequest;
import com.stoktakip.dto.ModelCreateRequest;
import com.stoktakip.dto.ModelUpdateRequest;
import com.stoktakip.entity.Marka;
import com.stoktakip.entity.Model;
import com.stoktakip.exception.BusinessRuleException;
import com.stoktakip.exception.ResourceNotFoundException;
import com.stoktakip.repository.KategoriRepository;
import com.stoktakip.repository.MarkaRepository;
import com.stoktakip.repository.ModelRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MarkaModelServiceTest {

    @Mock MarkaRepository markaRepository;
    @Mock ModelRepository modelRepository;
    @Mock KategoriRepository kategoriRepository;

    private MarkaServiceImpl markaService;
    private ModelServiceImpl modelService;

    @BeforeEach
    void setUp() {
        markaService = new MarkaServiceImpl(markaRepository);
        modelService = new ModelServiceImpl(modelRepository, markaRepository, kategoriRepository);
    }

    @Test
    void tumMarkalarGetirilir() {
        when(markaRepository.findAll()).thenReturn(List.of(marka("m-1", "Bosch")));

        assertThat(markaService.getAll()).extracting(response -> response.markaAdi())
            .containsExactly("Bosch");
    }

    @Test
    void markaAramasiTrimEdilipRepositorySeviyesindeYapilir() {
        when(markaRepository.findByMarkaAdiContainingIgnoreCaseAndSilindiFalse("sAmS"))
            .thenReturn(List.of(marka("m-1", "Samsung")));

        assertThat(markaService.searchByName("  sAmS "))
            .extracting(response -> response.markaAdi()).containsExactly("Samsung");
        verify(markaRepository).findByMarkaAdiContainingIgnoreCaseAndSilindiFalse("sAmS");
    }

    @Test
    void bosMarkaAramasiReddedilir() {
        assertThatThrownBy(() -> markaService.searchByName("   "))
            .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void markaCreateUpdateVeDeleteCalisir() {
        when(markaRepository.save(any(Marka.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Marka mevcut = marka("m-1", "Eski");
        when(markaRepository.findById("m-1")).thenReturn(Optional.of(mevcut));

        var created = markaService.create(new MarkaCreateRequest(" Lenovo ", "Yeni"));
        var updated = markaService.update("m-1", new MarkaUpdateRequest(" Bosch ", "Güncel"));
        markaService.delete("m-1");

        assertThat(created.guid()).isNotBlank();
        assertThat(created.markaAdi()).isEqualTo("Lenovo");
        assertThat(updated.guid()).isEqualTo("m-1");
        assertThat(updated.markaAdi()).isEqualTo("Bosch");
        assertThat(updated.aciklama()).isEqualTo("Güncel");
        verify(markaRepository).delete(mevcut);
        verify(markaRepository).flush();
    }

    @Test
    void olmayanMarkaUpdateEdilemez() {
        when(markaRepository.findById("yok")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> markaService.update(
            "yok", new MarkaUpdateRequest("Bosch", null)))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void tumModellerGetirilir() {
        when(modelRepository.findAll()).thenReturn(List.of(model("model-1", "ThinkPad")));

        assertThat(modelService.getAll()).extracting(response -> response.modelAdi())
            .containsExactly("ThinkPad");
    }

    @Test
    void modelAramasiTrimEdilipRepositorySeviyesindeYapilir() {
        when(markaRepository.findById("marka-1"))
            .thenReturn(Optional.of(marka("marka-1", "Lenovo")));
        when(modelRepository.findByMarka_GuidAndModelAdiContainingIgnoreCaseAndSilindiFalse("marka-1", "tHiNk"))
            .thenReturn(List.of(model("model-1", "ThinkPad")));

        assertThat(modelService.searchByMarkaAndName(" marka-1 ", " tHiNk "))
            .extracting(response -> response.modelAdi()).containsExactly("ThinkPad");
        verify(modelRepository)
            .findByMarka_GuidAndModelAdiContainingIgnoreCaseAndSilindiFalse("marka-1", "tHiNk");
    }

    @Test
    void bosModelAramasiReddedilir() {
        assertThatThrownBy(() -> modelService.searchByMarkaAndName(null, "think"))
            .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void modelCreateUpdateVeDeleteCalisirVeMarkaIliskisiKorunur() {
        when(modelRepository.save(any(Model.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Marka eskiMarka = marka("marka-1", "Lenovo");
        Marka yeniMarka = marka("marka-2", "Dell");
        Model mevcut = model("model-1", "Eski Model");
        mevcut.setMarka(eskiMarka);
        when(modelRepository.findById("model-1")).thenReturn(Optional.of(mevcut));
        when(markaRepository.findById("marka-1")).thenReturn(Optional.of(eskiMarka));
        when(markaRepository.findById("marka-2")).thenReturn(Optional.of(yeniMarka));

        var created = modelService.create(new ModelCreateRequest(
            " ThinkBook ", null, "marka-1", null));
        var updated = modelService.update(
            "model-1", new ModelUpdateRequest(" ThinkPad ", "Güncel", "marka-2"));
        modelService.delete("model-1");

        assertThat(created.guid()).isNotBlank();
        assertThat(created.modelAdi()).isEqualTo("ThinkBook");
        assertThat(updated.guid()).isEqualTo("model-1");
        assertThat(updated.modelAdi()).isEqualTo("ThinkPad");
        assertThat(created.markaGuid()).isEqualTo("marka-1");
        assertThat(updated.markaGuid()).isEqualTo("marka-2");
        assertThat(updated.markaAdi()).isEqualTo("Dell");
        assertThat(mevcut.getMarka()).isSameAs(yeniMarka);
        verify(modelRepository).delete(mevcut);
        verify(modelRepository).flush();
    }

    @Test
    void olmayanModelUpdateEdilemez() {
        when(modelRepository.findById("yok")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> modelService.update(
            "yok", new ModelUpdateRequest("ThinkPad", null, "marka-1")))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void olmayanMarkaIleModelOlusturulamaz() {
        when(markaRepository.findById("yok")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> modelService.create(
            new ModelCreateRequest("ThinkPad", null, "yok", null)))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Marka bulunamadı");
    }

    @Test
    void olmayanMarkaninModelleriListelenemez() {
        when(markaRepository.findById("yok")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> modelService.getByMarkaGuid("yok"))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Marka bulunamadı");
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
