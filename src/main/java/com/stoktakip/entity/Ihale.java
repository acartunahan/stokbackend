package com.stoktakip.entity;

import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

@Entity
@Table(name = "IHALE")
public class Ihale {

    @Id
    @Column(name = "GUID", length = 128)
    private String guid;

    @Column(name = "OLUSTURMA_TARIHI")
    private LocalDateTime olusturmaTarihi;

    @Column(name = "DURUM", length = 32)
    private String durum;

    @Column(name = "AD", length = 100, nullable = false)
    private String ad;

    @Column(name = "IHALE_TARIHI")
    private LocalDateTime ihaleTarihi;

    @Column(name = "KABUL_TARIHI")
    private LocalDateTime kabulTarihi;

    @Column(name = "SOZLESME_TARIHI")
    private LocalDateTime sozlesmeTarihi;

    @Column(name = "BIRIM_KODU")
    private Long birimKodu;

    @Column(name = "PERSONNEL", length = 100)
    private String personel;

    @Column(name = "ACIKLAMA", length = 500)
    private String aciklama;

    @Column(name = "KOMISYON_BASKANI", length = 100)
    private String komisyonBaskani;

    @Column(name = "UYE1", length = 100)
    private String uye1;

    @Column(name = "UYE2", length = 100)
    private String uye2;

    @Column(name = "YERINDE_GARANTI")
    private Long yerindeGaranti;

    @Column(name = "ONARIM_SURESI")
    private Long onarimSuresi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FIRMA_GUID", referencedColumnName = "GUID")
    private Firma firma;

    @OneToMany(mappedBy = "ihale", fetch = FetchType.LAZY)
    private List<EnvanterDetay> envanterDetaylari = new ArrayList<>();

    @OneToMany(mappedBy = "ihale", fetch = FetchType.LAZY)
    private List<SayimDetay> sayimDetaylari = new ArrayList<>();

}
