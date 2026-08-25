package com.stoktakip.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "ENVANTER_DETAY")
public class EnvanterDetay extends BaseEntity{

    @Id
    @Column(name = "GUID", length = 128)
    private String guid;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ENVANTER_GUID", referencedColumnName = "GUID", nullable = false, unique = true)
    private Envanter envanter;

    @Column(name = "ALIM_YOLU")
    private Long alimYolu;

    @Column(name = "ALIM_TURU", length = 20)
    private String alimTuru;

    @Column(name = "SAYMANLIK_NO", length = 100)
    private String saymanlikNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IHALEGUID", referencedColumnName = "GUID")
    private Ihale ihale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FIRMAGUID", referencedColumnName = "GUID")
    private Firma firma;

    @Column(name = "GARANTI_BASLANGIC_TARIHI")
    private LocalDateTime garantiBaslangicTarihi;

    @Column(name = "GARANTI_BITIS_TARIHI")
    private LocalDateTime garantiBitisTarihi;

    public String getGuid() { return guid; }
    public void setGuid(String guid) { this.guid = guid; }
    public Envanter getEnvanter() { return envanter; }
    public void setEnvanter(Envanter envanter) { this.envanter = envanter; }
    public Long getAlimYolu() { return alimYolu; }
    public void setAlimYolu(Long alimYolu) { this.alimYolu = alimYolu; }
    public String getAlimTuru() { return alimTuru; }
    public void setAlimTuru(String alimTuru) { this.alimTuru = alimTuru; }
    public String getSaymanlikNo() { return saymanlikNo; }
    public void setSaymanlikNo(String saymanlikNo) { this.saymanlikNo = saymanlikNo; }
    public Ihale getIhale() { return ihale; }
    public void setIhale(Ihale ihale) { this.ihale = ihale; }
    public Firma getFirma() { return firma; }
    public void setFirma(Firma firma) { this.firma = firma; }
    public LocalDateTime getGarantiBaslangicTarihi() { return garantiBaslangicTarihi; }
    public void setGarantiBaslangicTarihi(LocalDateTime value) { this.garantiBaslangicTarihi = value; }
    public LocalDateTime getGarantiBitisTarihi() { return garantiBitisTarihi; }
    public void setGarantiBitisTarihi(LocalDateTime value) { this.garantiBitisTarihi = value; }
}
