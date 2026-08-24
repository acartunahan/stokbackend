package com.stoktakip.entity;

import jakarta.persistence.Id;
import com.stoktakip.enums.EnvanterTuru;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;


@Entity
@Table(name = "ENVANTER")
public class Envanter {

    @Id
    @Column(name = "GUID", length = 128)
    private String guid;

    @Column(name = "DEMIRBAS_NUMARASI")
    private Long demirbasNumarasi;

    @Column(name = "SERI_NO", length = 100)
    private String seriNo;

    @Column(name = "BARKOD", length = 100)
    private String barkod;

    //birim
    @Column(name = "SUBE")
    private Long sube;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEPO_GUID", referencedColumnName = "GUID")
    private Depo depo;

    @Column(name = "ENVANTER_ADI", length = 255, nullable = false)
    private String envanterAdi;

    @Column(name = "ACIKLAMA", length = 1000)
    private String aciklama;

    @Column(name = "IL_SUBE_DAIRE_BASKANLIK_NUMARASI")
    private String ilSubeDaireBaskanlikNumarasi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MODEL_GUID", referencedColumnName = "GUID")
    private Model model;

    @OneToOne(mappedBy = "envanter", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private EnvanterDetay detay;

    @Enumerated(EnumType.STRING)
    private EnvanterTuru envanterTuru;

    @OneToMany(mappedBy = "envanter", fetch = FetchType.LAZY)
    private List<EnvanterAciklama> envanterAciklamalari = new ArrayList<>();

    @OneToMany(mappedBy = "envanter", fetch = FetchType.LAZY)
    private List<DepoZimmetHareket> depoZimmetHareketleri = new ArrayList<>();

    @OneToMany(mappedBy = "envanter", fetch = FetchType.LAZY)
    private List<PersonelZimmetHareket> personelZimmetHareketleri = new ArrayList<>();

    @OneToMany(mappedBy = "envanter", fetch = FetchType.LAZY)
    private List<SayimDetay> sayimDetaylari = new ArrayList<>();

    @OneToMany(mappedBy = "envanter", fetch = FetchType.LAZY)
    private List<IadeHavuzKaydi> iadeHavuzKayitlari = new ArrayList<>();

    @OneToMany(mappedBy = "envanter", fetch = FetchType.LAZY)
    private List<IadeOnayKaydi> iadeOnayKayitlari = new ArrayList<>();

    @OneToMany(mappedBy = "envanter", fetch = FetchType.LAZY)
    private List<Aparat> aparatlar = new ArrayList<>();

    public String getGuid() { return guid; }
    public void setGuid(String guid) { this.guid = guid; }
    public Depo getDepo() { return depo; }
    public void setDepo(Depo depo) { this.depo = depo; }
    public EnvanterTuru getEnvanterTuru() { return envanterTuru; }
    public void setEnvanterTuru(EnvanterTuru envanterTuru) { this.envanterTuru = envanterTuru; }
    public String getEnvanterAdi() { return envanterAdi; }
    public void setEnvanterAdi(String envanterAdi) { this.envanterAdi = envanterAdi; }
    public List<Aparat> getAparatlar() { return aparatlar; }
    public void setAparatlar(List<Aparat> aparatlar) { this.aparatlar = aparatlar; }
}
