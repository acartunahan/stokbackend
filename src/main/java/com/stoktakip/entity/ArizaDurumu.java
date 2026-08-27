package com.stoktakip.entity;

import com.stoktakip.enums.ArizaDurumTipi;
import com.stoktakip.enums.TamirSonucu;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ARIZA_DURUMU")
public class ArizaDurumu extends SoftDeleteEntity {
    @Id
    @Column(name = "GUID", length = 128)
    private String guid;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ARIZA_GUID", referencedColumnName = "GUID", nullable = false)
    private Ariza ariza;

    @Enumerated(EnumType.STRING)
    @Column(name = "DURUM_TIPI", length = 40, nullable = false)
    private ArizaDurumTipi durumTipi;

    @Column(name = "DURUM_TARIHI", nullable = false)
    private LocalDateTime durumTarihi;
    @Column(name = "ACIKLAMA", length = 2000)
    private String aciklama;
    @Column(name = "SERVIS_ADI", length = 255)
    private String servisAdi;
    @Column(name = "SERVIS_REFERANS_NO", length = 100)
    private String servisReferansNo;
    @Column(name = "KARGO_FIRMASI", length = 255)
    private String kargoFirmasi;
    @Column(name = "KARGO_TAKIP_NO", length = 100)
    private String kargoTakipNo;
    @Enumerated(EnumType.STRING)
    @Column(name = "TAMIR_SONUCU", length = 40)
    private TamirSonucu tamirSonucu;
    @Column(name = "TAMIR_ACIKLAMASI", length = 2000)
    private String tamirAciklamasi;
    @Column(name = "MASRAF", precision = 19, scale = 2)
    private BigDecimal masraf;
    @Column(name = "PARA_BIRIMI", length = 3)
    private String paraBirimi;
    @Column(name = "DURUM", length = 32, nullable = false)
    private String durum = "AKTIF";
    @Column(name = "K_TARIH", nullable = false)
    private LocalDateTime kayitTarihi;

    @PrePersist
    void defaults() {
        if (durum == null) durum = "AKTIF";
        if (kayitTarihi == null) kayitTarihi = LocalDateTime.now();
    }

    public String getGuid() { return guid; }
    public void setGuid(String guid) { this.guid = guid; }
    public Ariza getAriza() { return ariza; }
    public void setAriza(Ariza ariza) { this.ariza = ariza; }
    public ArizaDurumTipi getDurumTipi() { return durumTipi; }
    public void setDurumTipi(ArizaDurumTipi value) { this.durumTipi = value; }
    public LocalDateTime getDurumTarihi() { return durumTarihi; }
    public void setDurumTarihi(LocalDateTime value) { this.durumTarihi = value; }
    public String getAciklama() { return aciklama; }
    public void setAciklama(String value) { this.aciklama = value; }
    public String getServisAdi() { return servisAdi; }
    public void setServisAdi(String value) { this.servisAdi = value; }
    public String getServisReferansNo() { return servisReferansNo; }
    public void setServisReferansNo(String value) { this.servisReferansNo = value; }
    public String getKargoFirmasi() { return kargoFirmasi; }
    public void setKargoFirmasi(String value) { this.kargoFirmasi = value; }
    public String getKargoTakipNo() { return kargoTakipNo; }
    public void setKargoTakipNo(String value) { this.kargoTakipNo = value; }
    public TamirSonucu getTamirSonucu() { return tamirSonucu; }
    public void setTamirSonucu(TamirSonucu value) { this.tamirSonucu = value; }
    public String getTamirAciklamasi() { return tamirAciklamasi; }
    public void setTamirAciklamasi(String value) { this.tamirAciklamasi = value; }
    public BigDecimal getMasraf() { return masraf; }
    public void setMasraf(BigDecimal value) { this.masraf = value; }
    public String getParaBirimi() { return paraBirimi; }
    public void setParaBirimi(String value) { this.paraBirimi = value; }
    public String getDurum() { return durum; }
    public void setDurum(String value) { this.durum = value; }
    public LocalDateTime getKayitTarihi() { return kayitTarihi; }
}
