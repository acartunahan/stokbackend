package com.stoktakip.entity;

import com.stoktakip.enums.ZimmetHareketTipi;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "DEPO_ZIMMET_HAREKET")
public class DepoZimmetHareket extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "depo_zimmet_hareket_seq")
    @SequenceGenerator(
        name = "depo_zimmet_hareket_seq",
        sequenceName = "DEPO_ZIMMET_HAREKET_SEQ",
        allocationSize = 1
    )
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ENVANTER_GUID", referencedColumnName = "GUID")
    private Envanter envanter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APARAT_GUID", referencedColumnName = "GUID")
    private Aparat aparat;

    @Enumerated(EnumType.STRING)
    @Column(name = "HAREKET_TIPI", length = 32)
    private ZimmetHareketTipi hareketTipi;

    @Column(name = "ZIMMET_TURU", length = 100)
    private String zimmetTuru;

    @Column(name = "ADET")
    private Long adet;

    @Column(name = "TUR", length = 20)
    private String tur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEVCUT_DEPO_GUID", referencedColumnName = "GUID")
    private Depo mevcutDepo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GIDECEGI_DEPO_GUID", referencedColumnName = "GUID")
    private Depo gidecegiDepo;

    @Column(name = "ACIKLAMA", length = 1000)
    private String aciklama;

    @Column(name = "ISLEM_YAPAN_PERSONEL", length = 100)
    private String islemYapanPersonel;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Envanter getEnvanter() {
        return envanter;
    }

    public void setEnvanter(Envanter envanter) {
        this.envanter = envanter;
    }

    public Aparat getAparat() {
        return aparat;
    }

    public void setAparat(Aparat aparat) {
        this.aparat = aparat;
    }

    public ZimmetHareketTipi getHareketTipi() { return hareketTipi; }
    public void setHareketTipi(ZimmetHareketTipi hareketTipi) { this.hareketTipi = hareketTipi; }

    public String getZimmetTuru() {
        return zimmetTuru;
    }

    public void setZimmetTuru(String zimmetTuru) {
        this.zimmetTuru = zimmetTuru;
    }

    public Long getAdet() {
        return adet;
    }

    public void setAdet(Long adet) {
        this.adet = adet;
    }

    public String getTur() {
        return tur;
    }

    public void setTur(String tur) {
        this.tur = tur;
    }

    public Depo getMevcutDepo() {
        return mevcutDepo;
    }

    public void setMevcutDepo(Depo mevcutDepo) {
        this.mevcutDepo = mevcutDepo;
    }

    public Depo getGidecegiDepo() {
        return gidecegiDepo;
    }

    public void setGidecegiDepo(Depo gidecegiDepo) {
        this.gidecegiDepo = gidecegiDepo;
    }

    public String getAciklama() {
        return aciklama;
    }

    public void setAciklama(String aciklama) {
        this.aciklama = aciklama;
    }

    public String getIslemYapanPersonel() {
        return islemYapanPersonel;
    }

    public void setIslemYapanPersonel(String islemYapanPersonel) {
        this.islemYapanPersonel = islemYapanPersonel;
    }

    @PrePersist
    @PreUpdate
    private void varlikIliskisiniDogrula() {
        if ((envanter == null) == (aparat == null)) {
            throw new IllegalStateException("Depo zimmet hareketi tam olarak bir varliga bagli olmalidir");
        }
    }
}
