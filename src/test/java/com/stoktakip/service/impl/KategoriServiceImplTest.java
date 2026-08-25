package com.stoktakip.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stoktakip.dto.KategoriRequest;
import com.stoktakip.entity.Kategori;
import com.stoktakip.entity.Model;
import com.stoktakip.exception.BusinessRuleException;
import com.stoktakip.repository.KategoriRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KategoriServiceImplTest {

    @Mock
    private KategoriRepository repository;

    private KategoriServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new KategoriServiceImpl(repository);
    }

    @Test
    void parentIleKategoriOlustururVeGuidUretir() {
        Kategori parent = kategori("parent-1", "Donanım");
        when(repository.findById("parent-1")).thenReturn(Optional.of(parent));
        when(repository.save(any(Kategori.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create(new KategoriRequest("  Bilgisayar  ", " parent-1 "));

        ArgumentCaptor<Kategori> captor = ArgumentCaptor.forClass(Kategori.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getGuid()).isNotBlank();
        assertThat(captor.getValue().getParent()).isSameAs(parent);
        assertThat(response.baslik()).isEqualTo("Bilgisayar");
        assertThat(response.parentId()).isEqualTo("parent-1");
    }

    @Test
    void kategoriKendisineParentOlamaz() {
        Kategori kategori = kategori("kategori-1", "Kategori");
        when(repository.findById("kategori-1")).thenReturn(Optional.of(kategori));

        assertThatThrownBy(() -> service.update(
            "kategori-1", new KategoriRequest("Yeni", "kategori-1")))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("kendi üst");

        verify(repository, never()).save(any());
    }

    @Test
    void kategoriKendiAltSoyunaBaglanamaz() {
        Kategori root = kategori("root", "Root");
        Kategori child = kategori("child", "Child");
        child.setParent(root);
        when(repository.findById("root")).thenReturn(Optional.of(root));
        when(repository.findById("child")).thenReturn(Optional.of(child));

        assertThatThrownBy(() -> service.update(
            "root", new KategoriRequest("Root", "child")))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("alt kategorisine");
    }

    @Test
    void altKategorisiBulunanKategoriSilinemez() {
        Kategori root = kategori("root", "Root");
        root.setAltKategoriler(List.of(kategori("child", "Child")));
        when(repository.findById("root")).thenReturn(Optional.of(root));

        assertThatThrownBy(() -> service.delete("root"))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("Alt kategorisi");

        verify(repository, never()).delete(any());
    }

    @Test
    void modelAtanmisKategoriSilinemez() {
        Kategori kategori = kategori("kategori-1", "Kategori");
        kategori.setModeller(List.of(new Model()));
        when(repository.findById("kategori-1")).thenReturn(Optional.of(kategori));

        assertThatThrownBy(() -> service.delete("kategori-1"))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("Model atanmış");
    }

    @Test
    void bosKategoriSilinirVeConstraintAyniTransactiondaKontrolEdilir() {
        Kategori kategori = kategori("kategori-1", "Kategori");
        when(repository.findById("kategori-1")).thenReturn(Optional.of(kategori));

        service.delete("kategori-1");

        verify(repository).delete(kategori);
        verify(repository).flush();
    }

    @Test
    void kategoriAgaciniNestedDtoOlarakOlusturur() {
        Kategori root = kategori("root", "Root");
        Kategori child = kategori("child", "Child");
        Kategori grandchild = kategori("grandchild", "Grandchild");
        child.setAltKategoriler(new ArrayList<>(List.of(grandchild)));
        root.setAltKategoriler(new ArrayList<>(List.of(child)));
        when(repository.findByParentIsNull()).thenReturn(List.of(root));

        var tree = service.getCategoryTree();

        assertThat(tree).hasSize(1);
        assertThat(tree.getFirst().altKategoriler()).hasSize(1);
        assertThat(tree.getFirst().altKategoriler().getFirst().altKategoriler().getFirst().guid())
            .isEqualTo("grandchild");
    }

    @Test
    void bozukVerideTreeMappingSonsuzDonguyeGirmez() {
        Kategori first = kategori("first", "First");
        Kategori second = kategori("second", "Second");
        first.setAltKategoriler(List.of(second));
        second.setAltKategoriler(List.of(first));
        when(repository.findByParentIsNull()).thenReturn(List.of(first));

        assertThatThrownBy(service::getCategoryTree)
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("döngü");
    }

    private static Kategori kategori(String guid, String baslik) {
        Kategori kategori = new Kategori();
        kategori.setGuid(guid);
        kategori.setBaslik(baslik);
        return kategori;
    }
}
