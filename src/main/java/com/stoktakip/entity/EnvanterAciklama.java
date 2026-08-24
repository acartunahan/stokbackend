package com.stoktakip.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "ENVANTER_ACIKLAMA")
public class EnvanterAciklama {

    @Column(name = "ID")
    private Long id;

    @Id
    @Column(name = "GUID", length = 128)
    private String guid;

    @Column(name = "OLUSTURMA_TARIHI")
    private LocalDateTime olusturmaTarihi;

    @Column(name = "DURUM", length = 32)
    private String durum;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public LocalDateTime getOlusturmaTarihi() {
        return olusturmaTarihi;
    }

    public void setOlusturmaTarihi(LocalDateTime olusturmaTarihi) {
        this.olusturmaTarihi = olusturmaTarihi;
    }

    public String getDurum() {
        return durum;
    }

    public void setDurum(String durum) {
        this.durum = durum;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "R_DEMIRBASGUID", referencedColumnName = "GUID")
    private Envanter envanter;

    @Lob
    @Column(name = "NOT_METNI")
    private String notMetni;

    @Column(name = "ZIMMETLI_PERSONEL_METNI", length = 500)
    private String zimmetliPersonelMetni;

    @Column(name = "EKLEYEN", length = 150)
    private String ekleyen;

    @Column(name = "EKLEYEN_PERSONEL", length = 20)
    private String ekleyenPersonel;

    public Envanter getEnvanter() {
        return envanter;
    }

    public void setEnvanter(Envanter envanter) {
        this.envanter = envanter;
    }

    public String getNotMetni() {
        return notMetni;
    }

    public void setNotMetni(String notMetni) {
        this.notMetni = notMetni;
    }

    public String getZimmetliPersonelMetni() {
        return zimmetliPersonelMetni;
    }

    public void setZimmetliPersonelMetni(String zimmetliPersonelMetni) {
        this.zimmetliPersonelMetni = zimmetliPersonelMetni;
    }

    public String getEkleyen() {
        return ekleyen;
    }

    public void setEkleyen(String ekleyen) {
        this.ekleyen = ekleyen;
    }

    public String getEkleyenPersonel() {
        return ekleyenPersonel;
    }

    public void setEkleyenPersonel(String ekleyenPersonel) {
        this.ekleyenPersonel = ekleyenPersonel;
    }
}
