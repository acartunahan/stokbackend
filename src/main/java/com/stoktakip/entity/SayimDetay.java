package com.stoktakip.entity;

import jakarta.persistence.Id;
import com.stoktakip.enums.SayimIslemi;
import com.stoktakip.enums.SayimKapsami;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "SAYIM_DETAY")
public class SayimDetay {

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
    @JoinColumn(name = "R_SAYIMGUID", referencedColumnName = "GUID")
    private Sayim sayim;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "R_DEMIRBASGUID", referencedColumnName = "GUID")
    private Envanter envanter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "R_DEPOGUID", referencedColumnName = "GUID")
    private Depo depo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MARKAGUID", referencedColumnName = "GUID")
    private Marka marka;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MODELGUID", referencedColumnName = "GUID")
    private Model model;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IHALEGUID", referencedColumnName = "GUID")
    private Ihale ihale;

    @Enumerated(EnumType.STRING)
    @Column(name = "SAYIM_KAPSAMI", length = 32)
    private SayimKapsami sayimKapsami;

    @Enumerated(EnumType.STRING)
    @Column(name = "ISLEM", length = 32)
    private SayimIslemi islem;

    @Column(name = "SAYIM_TARIHI")
    private LocalDateTime sayimTarihi;

    @Column(name = "ANLIK_AD", length = 255)
    private String anlikAd;

    @Column(name = "ZIMMETLI_PERSONEL_KODU", length = 100)
    private String zimmetliPersonelKodu;

    @Column(name = "GARANTI_BASLANGIC_TARIHI")
    private LocalDateTime garantiBaslangicTarihi;

    @Column(name = "GARANTI_BITIS_TARIHI")
    private LocalDateTime garantiBitisTarihi;

    @Column(name = "ACIKLAMA", length = 500)
    private String aciklama;

    @Column(name = "SAYMANLIK_NO", length = 100)
    private String saymanlikNo;

    @Column(name = "SUBE_NO", length = 100)
    private String subeNo;

    @Column(name = "ALIM_TURU", length = 20)
    private String alimTuru;

    public Sayim getSayim() {
        return sayim;
    }

    public void setSayim(Sayim sayim) {
        this.sayim = sayim;
    }

    public Envanter getEnvanter() {
        return envanter;
    }

    public void setEnvanter(Envanter envanter) {
        this.envanter = envanter;
    }

    public Depo getDepo() {
        return depo;
    }

    public void setDepo(Depo depo) {
        this.depo = depo;
    }

    public Marka getMarka() {
        return marka;
    }

    public void setMarka(Marka marka) {
        this.marka = marka;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public Ihale getIhale() {
        return ihale;
    }

    public void setIhale(Ihale ihale) {
        this.ihale = ihale;
    }

    public SayimKapsami getSayimKapsami() {
        return sayimKapsami;
    }

    public void setSayimKapsami(SayimKapsami sayimKapsami) {
        this.sayimKapsami = sayimKapsami;
    }

    public SayimIslemi getIslem() {
        return islem;
    }

    public void setIslem(SayimIslemi islem) {
        this.islem = islem;
    }

    public LocalDateTime getSayimTarihi() {
        return sayimTarihi;
    }

    public void setSayimTarihi(LocalDateTime sayimTarihi) {
        this.sayimTarihi = sayimTarihi;
    }

    public String getAnlikAd() {
        return anlikAd;
    }

    public void setAnlikAd(String anlikAd) {
        this.anlikAd = anlikAd;
    }

    public String getZimmetliPersonelKodu() {
        return zimmetliPersonelKodu;
    }

    public void setZimmetliPersonelKodu(String zimmetliPersonelKodu) {
        this.zimmetliPersonelKodu = zimmetliPersonelKodu;
    }

    public LocalDateTime getGarantiBaslangicTarihi() {
        return garantiBaslangicTarihi;
    }

    public void setGarantiBaslangicTarihi(LocalDateTime garantiBaslangicTarihi) {
        this.garantiBaslangicTarihi = garantiBaslangicTarihi;
    }

    public LocalDateTime getGarantiBitisTarihi() {
        return garantiBitisTarihi;
    }

    public void setGarantiBitisTarihi(LocalDateTime garantiBitisTarihi) {
        this.garantiBitisTarihi = garantiBitisTarihi;
    }

    public String getAciklama() {
        return aciklama;
    }

    public void setAciklama(String aciklama) {
        this.aciklama = aciklama;
    }

    public String getSaymanlikNo() {
        return saymanlikNo;
    }

    public void setSaymanlikNo(String saymanlikNo) {
        this.saymanlikNo = saymanlikNo;
    }

    public String getSubeNo() {
        return subeNo;
    }

    public void setSubeNo(String subeNo) {
        this.subeNo = subeNo;
    }

    public String getAlimTuru() {
        return alimTuru;
    }

    public void setAlimTuru(String alimTuru) {
        this.alimTuru = alimTuru;
    }
}
