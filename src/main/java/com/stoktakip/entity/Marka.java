package com.stoktakip.entity;

import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

@Entity
@Table(name = "MARKA")
public class Marka extends BaseEntity{

    @Id
    @Column(name = "GUID", length = 128)
    private String guid;

    @Column(name = "MARKA_ADI", length = 255, nullable = false)
    private String markaAdi;

    @Column(name = "ACIKLAMA", length = 500)
    private String aciklama;

    @OneToMany(mappedBy = "marka", fetch = FetchType.LAZY)
    private List<Model> modeller = new ArrayList<>();

    @OneToMany(mappedBy = "marka", fetch = FetchType.LAZY)
    private List<SayimDetay> sayimDetaylari = new ArrayList<>();

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getMarkaAdi() {
        return markaAdi;
    }

    public void setMarkaAdi(String markaAdi) {
        this.markaAdi = markaAdi;
    }

    public String getAciklama() {
        return aciklama;
    }

    public void setAciklama(String aciklama) {
        this.aciklama = aciklama;
    }

}
