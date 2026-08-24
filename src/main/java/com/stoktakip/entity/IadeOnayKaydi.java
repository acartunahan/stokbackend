package com.stoktakip.entity;

import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "IADE_ONAY_KAYDI")
public class IadeOnayKaydi {

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SUANKIDEPOGUID", referencedColumnName = "GUID")
    private Depo suankiDepo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GIDECEGIDEPOGUID", referencedColumnName = "GUID")
    private Depo gidecegiDepo;

    public Envanter getEnvanter() {
        return envanter;
    }

    public void setEnvanter(Envanter envanter) {
        this.envanter = envanter;
    }

    public Depo getSuankiDepo() {
        return suankiDepo;
    }

    public void setSuankiDepo(Depo suankiDepo) {
        this.suankiDepo = suankiDepo;
    }

    public Depo getGidecegiDepo() {
        return gidecegiDepo;
    }

    public void setGidecegiDepo(Depo gidecegiDepo) {
        this.gidecegiDepo = gidecegiDepo;
    }
}
