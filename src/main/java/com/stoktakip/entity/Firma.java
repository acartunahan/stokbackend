package com.stoktakip.entity;

import jakarta.persistence.Id;
import com.stoktakip.enums.EnvanterTuru;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

@Entity
@Table(name = "FIRMA")
public class Firma {

    @Id
    @Column(name = "GUID", length = 128)
    private String guid;

    @Column(name = "OLUSTURMA_TARIHI")
    private LocalDateTime olusturmaTarihi;

    @Column(name = "DURUM", length = 32)
    private String durum;

    @Column(name = "AD", length = 255, nullable = false)
    private String ad;

    @Column(name = "YETKILI_KISI", length = 255)
    private String yetkiliKisi;

    @Column(name = "YETKILI_TELEFON", length = 50)
    private String yetkiliTelefon;

    @Column(name = "IRTIBAT_KISI", length = 255)
    private String irtibatKisi;

    @Column(name = "IRTIBAT_TELEFON", length = 255)
    private String irtibatTelefon;

    @Column(name = "TELEFON1", length = 50)
    private String telefon1;

    @Column(name = "TELEFON2", length = 50)
    private String telefon2;

    @Column(name = "FAX", length = 50)
    private String faks;

    @Column(name = "WEB", length = 255)
    private String webAdresi;

    @Column(name = "ADRES", length = 500)
    private String adres;

    @Column(name = "ACIKLAMA", length = 500)
    private String aciklama;

    @Enumerated(EnumType.STRING)
    @Column(name = "KAYNAK_ALANI", length = 32, nullable = false)
    private EnvanterTuru kaynakAlani;

    @OneToMany(mappedBy = "firma", fetch = FetchType.LAZY)
    private List<Ihale> ihaleler = new ArrayList<>();

    @OneToMany(mappedBy = "firma", fetch = FetchType.LAZY)
    private List<EnvanterDetay> envanterDetaylari = new ArrayList<>();
}
