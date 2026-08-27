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
@Table(name = "DEPO")
public class Depo extends SoftDeleteEntity {

    @Id
    @Column(name = "GUID", length = 128)
    private String guid;

    @Column(name = "OLUSTURMA_TARIHI")
    private LocalDateTime olusturmaTarihi;

    @Column(name = "DURUM", length = 32)
    private String durum;

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

    @Column(name = "AD", length = 100, nullable = false)
    private String ad;

    @Column(name = "ACIKLAMA", length = 500)
    private String aciklama;

    @Column(name = "PERSONEL_KODU", length = 20)
    private String personelKodu;

    @OneToMany(mappedBy = "depo", fetch = FetchType.LAZY)
    private List<Envanter> envanterler = new ArrayList<>();

    @OneToMany(mappedBy = "mevcutDepo", fetch = FetchType.LAZY)
    private List<DepoZimmetHareket> mevcutDepoZimmetHareketleri = new ArrayList<>();

    @OneToMany(mappedBy = "gidecegiDepo", fetch = FetchType.LAZY)
    private List<DepoZimmetHareket> gidecegiDepoZimmetHareketleri = new ArrayList<>();

    @OneToMany(mappedBy = "depo", fetch = FetchType.LAZY)
    private List<Sayim> sayimlar = new ArrayList<>();

    @OneToMany(mappedBy = "depo", fetch = FetchType.LAZY)
    private List<SayimDetay> sayimDetaylari = new ArrayList<>();

    @OneToMany(mappedBy = "depo", fetch = FetchType.LAZY)
    private List<SarfLogKaydi> sarfLogKayitlari = new ArrayList<>();

    public String getAd() {
        return ad;
    }

    public void setAd(String ad) {
        this.ad = ad;
    }

    public String getAciklama() {
        return aciklama;
    }

    public void setAciklama(String aciklama) {
        this.aciklama = aciklama;
    }

    public String getPersonelKodu() {
        return personelKodu;
    }

    public void setPersonelKodu(String personelKodu) {
        this.personelKodu = personelKodu;
    }

    public List<Envanter> getEnvanterler() {
        return envanterler;
    }

    public void setEnvanterler(List<Envanter> envanterler) {
        this.envanterler = envanterler;
    }

    public List<DepoZimmetHareket> getMevcutDepoZimmetHareketleri() {
        return mevcutDepoZimmetHareketleri;
    }

    public void setMevcutDepoZimmetHareketleri(List<DepoZimmetHareket> mevcutDepoZimmetHareketleri) {
        this.mevcutDepoZimmetHareketleri = mevcutDepoZimmetHareketleri;
    }

    public List<DepoZimmetHareket> getGidecegiDepoZimmetHareketleri() {
        return gidecegiDepoZimmetHareketleri;
    }

    public void setGidecegiDepoZimmetHareketleri(List<DepoZimmetHareket> gidecegiDepoZimmetHareketleri) {
        this.gidecegiDepoZimmetHareketleri = gidecegiDepoZimmetHareketleri;
    }

    public List<Sayim> getSayimlar() {
        return sayimlar;
    }

    public void setSayimlar(List<Sayim> sayimlar) {
        this.sayimlar = sayimlar;
    }

    public List<SayimDetay> getSayimDetaylari() {
        return sayimDetaylari;
    }

    public void setSayimDetaylari(List<SayimDetay> sayimDetaylari) {
        this.sayimDetaylari = sayimDetaylari;
    }

    public List<SarfLogKaydi> getSarfLogKayitlari() {
        return sarfLogKayitlari;
    }

    public void setSarfLogKayitlari(List<SarfLogKaydi> sarfLogKayitlari) {
        this.sarfLogKayitlari = sarfLogKayitlari;
    }
}
