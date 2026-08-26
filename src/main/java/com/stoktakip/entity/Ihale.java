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
@Table(name = "IHALE")
public class Ihale extends SoftDeleteEntity {

    @Id
    @Column(name = "GUID", length = 128)
    private String guid;

    @Column(name = "OLUSTURMA_TARIHI")
    private LocalDateTime olusturmaTarihi;

    @Column(name = "DURUM", length = 32)
    private String durum;

    @Column(name = "AD", length = 100, nullable = false)
    private String ad;

    @Column(name = "IHALE_TARIHI")
    private LocalDateTime ihaleTarihi;

    @Column(name = "KABUL_TARIHI")
    private LocalDateTime kabulTarihi;

    @Column(name = "SOZLESME_TARIHI")
    private LocalDateTime sozlesmeTarihi;

    @Column(name = "BIRIM_KODU")
    private Long birimKodu;

    @Column(name = "PERSONNEL", length = 100)
    private String personel;

    @Column(name = "ACIKLAMA", length = 500)
    private String aciklama;

    @Column(name = "KOMISYON_BASKANI", length = 100)
    private String komisyonBaskani;

    @Column(name = "UYE1", length = 100)
    private String uye1;

    @Column(name = "UYE2", length = 100)
    private String uye2;

    @Column(name = "YERINDE_GARANTI")
    private Long yerindeGaranti;

    @Column(name = "ONARIM_SURESI")
    private Long onarimSuresi;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "FIRMA_GUID", referencedColumnName = "GUID", nullable = false)
    private Firma firma;

    @OneToMany(mappedBy = "ihale", fetch = FetchType.LAZY)
    private List<EnvanterDetay> envanterDetaylari = new ArrayList<>();

    @OneToMany(mappedBy = "ihale", fetch = FetchType.LAZY)
    private List<SayimDetay> sayimDetaylari = new ArrayList<>();

    public String getGuid() { return guid; }
    public void setGuid(String guid) { this.guid = guid; }
    public LocalDateTime getOlusturmaTarihi() { return olusturmaTarihi; }
    public void setOlusturmaTarihi(LocalDateTime olusturmaTarihi) { this.olusturmaTarihi = olusturmaTarihi; }
    public String getDurum() { return durum; }
    public void setDurum(String durum) { this.durum = durum; }
    public String getAd() { return ad; }
    public void setAd(String ad) { this.ad = ad; }
    public LocalDateTime getIhaleTarihi() { return ihaleTarihi; }
    public void setIhaleTarihi(LocalDateTime ihaleTarihi) { this.ihaleTarihi = ihaleTarihi; }
    public LocalDateTime getKabulTarihi() { return kabulTarihi; }
    public void setKabulTarihi(LocalDateTime kabulTarihi) { this.kabulTarihi = kabulTarihi; }
    public LocalDateTime getSozlesmeTarihi() { return sozlesmeTarihi; }
    public void setSozlesmeTarihi(LocalDateTime sozlesmeTarihi) { this.sozlesmeTarihi = sozlesmeTarihi; }
    public Long getBirimKodu() { return birimKodu; }
    public void setBirimKodu(Long birimKodu) { this.birimKodu = birimKodu; }
    public String getPersonel() { return personel; }
    public void setPersonel(String personel) { this.personel = personel; }
    public String getAciklama() { return aciklama; }
    public void setAciklama(String aciklama) { this.aciklama = aciklama; }
    public String getKomisyonBaskani() { return komisyonBaskani; }
    public void setKomisyonBaskani(String komisyonBaskani) { this.komisyonBaskani = komisyonBaskani; }
    public String getUye1() { return uye1; }
    public void setUye1(String uye1) { this.uye1 = uye1; }
    public String getUye2() { return uye2; }
    public void setUye2(String uye2) { this.uye2 = uye2; }
    public Long getYerindeGaranti() { return yerindeGaranti; }
    public void setYerindeGaranti(Long yerindeGaranti) { this.yerindeGaranti = yerindeGaranti; }
    public Long getOnarimSuresi() { return onarimSuresi; }
    public void setOnarimSuresi(Long onarimSuresi) { this.onarimSuresi = onarimSuresi; }
    public Firma getFirma() { return firma; }
    public void setFirma(Firma firma) { this.firma = firma; }
    public List<EnvanterDetay> getEnvanterDetaylari() { return envanterDetaylari; }
    public void setEnvanterDetaylari(List<EnvanterDetay> envanterDetaylari) { this.envanterDetaylari = envanterDetaylari; }
    public List<SayimDetay> getSayimDetaylari() { return sayimDetaylari; }
    public void setSayimDetaylari(List<SayimDetay> sayimDetaylari) { this.sayimDetaylari = sayimDetaylari; }

}
