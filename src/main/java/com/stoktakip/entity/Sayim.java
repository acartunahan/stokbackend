package com.stoktakip.entity;

import jakarta.persistence.Id;
import com.stoktakip.enums.SayimKapsami;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Table;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

@Entity
@Table(name = "SAYIM")
public class Sayim {

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

    @Enumerated(EnumType.STRING)
    @Column(name = "SAYIM_KAPSAMI", length = 32)
    private SayimKapsami sayimKapsami;

    @Column(name = "BASLIK", length = 120)
    private String baslik;

    @Column(name = "START_DATE")
    private LocalDateTime baslangicTarihi;

    @Column(name = "END_DATE")
    private LocalDateTime bitisTarihi;

    @Column(name = "ESKI_DURUM_KODU")
    private Long eskiDurumKodu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEPO", referencedColumnName = "GUID")
    private Depo depo;

    @OneToMany(mappedBy = "sayim", fetch = FetchType.LAZY)
    private List<SayimDetay> sayimDetaylari = new ArrayList<>();

    public SayimKapsami getSayimKapsami() {
        return sayimKapsami;
    }

    public void setSayimKapsami(SayimKapsami sayimKapsami) {
        this.sayimKapsami = sayimKapsami;
    }

    public String getBaslik() {
        return baslik;
    }

    public void setBaslik(String baslik) {
        this.baslik = baslik;
    }

    public LocalDateTime getBaslangicTarihi() {
        return baslangicTarihi;
    }

    public void setBaslangicTarihi(LocalDateTime baslangicTarihi) {
        this.baslangicTarihi = baslangicTarihi;
    }

    public LocalDateTime getBitisTarihi() {
        return bitisTarihi;
    }

    public void setBitisTarihi(LocalDateTime bitisTarihi) {
        this.bitisTarihi = bitisTarihi;
    }

    public Long getEskiDurumKodu() {
        return eskiDurumKodu;
    }

    public void setEskiDurumKodu(Long eskiDurumKodu) {
        this.eskiDurumKodu = eskiDurumKodu;
    }

    public Depo getDepo() {
        return depo;
    }

    public void setDepo(Depo depo) {
        this.depo = depo;
    }

    public List<SayimDetay> getSayimDetaylari() {
        return sayimDetaylari;
    }

    public void setSayimDetaylari(List<SayimDetay> sayimDetaylari) {
        this.sayimDetaylari = sayimDetaylari;
    }
}
