package com.stoktakip.entity;

import com.stoktakip.enums.ArizaDurumTipi;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ARIZA")
public class Ariza extends SoftDeleteEntity {
    @Id
    @Column(name = "GUID", length = 128)
    private String guid;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ENVANTER_GUID", referencedColumnName = "GUID", nullable = false)
    private Envanter envanter;

    @Column(name = "ARIZA_TARIHI", nullable = false)
    private LocalDateTime arizaTarihi;

    @Column(name = "ARIZA_ACIKLAMASI", length = 2000, nullable = false)
    private String arizaAciklamasi;

    @Column(name = "BILDIRIM_YAPAN_PERSONEL_KODU", length = 100)
    private String bildirimYapanPersonelKodu;

    @Enumerated(EnumType.STRING)
    @Column(name = "SON_DURUM", length = 40, nullable = false)
    private ArizaDurumTipi sonDurum;

    @Column(name = "KAPANDI_MI", nullable = false)
    private Boolean kapandiMi = Boolean.FALSE;

    @Column(name = "KAPANIS_TARIHI")
    private LocalDateTime kapanisTarihi;

    @Column(name = "ACIKLAMA", length = 2000)
    private String aciklama;

    @Column(name = "DURUM", length = 32, nullable = false)
    private String durum = "AKTIF";

    @Column(name = "K_TARIH", nullable = false)
    private LocalDateTime kayitTarihi;

    @OneToMany(mappedBy = "ariza", fetch = FetchType.LAZY)
    private List<ArizaDurumu> durumGecmisi = new ArrayList<>();

    @PrePersist
    void defaults() {
        if (kapandiMi == null) kapandiMi = Boolean.FALSE;
        if (durum == null) durum = "AKTIF";
        if (kayitTarihi == null) kayitTarihi = LocalDateTime.now();
    }

    public String getGuid() { return guid; }
    public void setGuid(String guid) { this.guid = guid; }
    public Envanter getEnvanter() { return envanter; }
    public void setEnvanter(Envanter envanter) { this.envanter = envanter; }
    public LocalDateTime getArizaTarihi() { return arizaTarihi; }
    public void setArizaTarihi(LocalDateTime arizaTarihi) { this.arizaTarihi = arizaTarihi; }
    public String getArizaAciklamasi() { return arizaAciklamasi; }
    public void setArizaAciklamasi(String value) { this.arizaAciklamasi = value; }
    public String getBildirimYapanPersonelKodu() { return bildirimYapanPersonelKodu; }
    public void setBildirimYapanPersonelKodu(String value) { this.bildirimYapanPersonelKodu = value; }
    public ArizaDurumTipi getSonDurum() { return sonDurum; }
    public void setSonDurum(ArizaDurumTipi sonDurum) { this.sonDurum = sonDurum; }
    public Boolean getKapandiMi() { return kapandiMi; }
    public void setKapandiMi(Boolean kapandiMi) { this.kapandiMi = kapandiMi; }
    public LocalDateTime getKapanisTarihi() { return kapanisTarihi; }
    public void setKapanisTarihi(LocalDateTime value) { this.kapanisTarihi = value; }
    public String getAciklama() { return aciklama; }
    public void setAciklama(String aciklama) { this.aciklama = aciklama; }
    public String getDurum() { return durum; }
    public void setDurum(String durum) { this.durum = durum; }
    public LocalDateTime getKayitTarihi() { return kayitTarihi; }
    public List<ArizaDurumu> getDurumGecmisi() { return durumGecmisi; }
}
