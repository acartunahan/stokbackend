package com.stoktakip.entity;

import jakarta.persistence.Id;
import com.stoktakip.enums.EnvanterTuru;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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

    public String getGuid() { return guid; }
    public void setGuid(String guid) { this.guid = guid; }
    public LocalDateTime getOlusturmaTarihi() { return olusturmaTarihi; }
    public void setOlusturmaTarihi(LocalDateTime olusturmaTarihi) { this.olusturmaTarihi = olusturmaTarihi; }
    public String getDurum() { return durum; }
    public void setDurum(String durum) { this.durum = durum; }
    public String getAd() { return ad; }
    public void setAd(String ad) { this.ad = ad; }
    public String getYetkiliKisi() { return yetkiliKisi; }
    public void setYetkiliKisi(String yetkiliKisi) { this.yetkiliKisi = yetkiliKisi; }
    public String getYetkiliTelefon() { return yetkiliTelefon; }
    public void setYetkiliTelefon(String yetkiliTelefon) { this.yetkiliTelefon = yetkiliTelefon; }
    public String getIrtibatKisi() { return irtibatKisi; }
    public void setIrtibatKisi(String irtibatKisi) { this.irtibatKisi = irtibatKisi; }
    public String getIrtibatTelefon() { return irtibatTelefon; }
    public void setIrtibatTelefon(String irtibatTelefon) { this.irtibatTelefon = irtibatTelefon; }
    public String getTelefon1() { return telefon1; }
    public void setTelefon1(String telefon1) { this.telefon1 = telefon1; }
    public String getTelefon2() { return telefon2; }
    public void setTelefon2(String telefon2) { this.telefon2 = telefon2; }
    public String getFaks() { return faks; }
    public void setFaks(String faks) { this.faks = faks; }
    public String getWebAdresi() { return webAdresi; }
    public void setWebAdresi(String webAdresi) { this.webAdresi = webAdresi; }
    public String getAdres() { return adres; }
    public void setAdres(String adres) { this.adres = adres; }
    public String getAciklama() { return aciklama; }
    public void setAciklama(String aciklama) { this.aciklama = aciklama; }
    public EnvanterTuru getKaynakAlani() { return kaynakAlani; }
    public void setKaynakAlani(EnvanterTuru kaynakAlani) { this.kaynakAlani = kaynakAlani; }
    public List<Ihale> getIhaleler() { return ihaleler; }
    public void setIhaleler(List<Ihale> ihaleler) { this.ihaleler = ihaleler; }
    public List<EnvanterDetay> getEnvanterDetaylari() { return envanterDetaylari; }
    public void setEnvanterDetaylari(List<EnvanterDetay> envanterDetaylari) { this.envanterDetaylari = envanterDetaylari; }
}
