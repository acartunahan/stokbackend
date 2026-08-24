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
@Table(name = "MODEL")
public class Model extends BaseEntity{

    @Id
    @Column(name = "GUID", length = 128)
    private String guid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MARKA_GUID", referencedColumnName = "GUID")
    private Marka marka;

    @Column(name = "MODEL_ADI", length = 255, nullable = false)
    private String modelAdi;

    @Column(name = "ACIKLAMA", length = 500)
    private String aciklama;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "KATEGORI_GUID", referencedColumnName = "GUID")
    private Kategori kategori;

    @OneToMany(mappedBy = "model", fetch = FetchType.LAZY)
    private List<Envanter> envanterler = new ArrayList<>();

    @OneToMany(mappedBy = "model", fetch = FetchType.LAZY)
    private List<SayimDetay> sayimDetaylari = new ArrayList<>();

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public Marka getMarka() {
        return marka;
    }

    public void setMarka(Marka marka) {
        this.marka = marka;
    }

    public String getModelAdi() {
        return modelAdi;
    }

    public void setModelAdi(String modelAdi) {
        this.modelAdi = modelAdi;
    }

    public String getAciklama() {
        return aciklama;
    }

    public void setAciklama(String aciklama) {
        this.aciklama = aciklama;
    }

    public Kategori getKategori() {
        return kategori;
    }

    public void setKategori(Kategori kategori) {
        this.kategori = kategori;
    }

}
