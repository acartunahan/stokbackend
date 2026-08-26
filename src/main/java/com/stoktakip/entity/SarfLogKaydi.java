package com.stoktakip.entity;

import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "SARF_LOG_KAYDI")
public class SarfLogKaydi extends SoftDeleteEntity {

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

    @Column(name = "PERSONEL_KODU", length = 50)
    private String personelKodu;

    @Lob
    @Column(name = "ACIKLAMA")
    private String aciklama;

    @Column(name = "ISLEM", length = 20)
    private String islem;

    @Column(name = "SARF_MALZEME_GUID", length = 64)
    private String sarfMalzemeGuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RESAT_DEPOGUID", referencedColumnName = "GUID")
    private Depo depo;

    public String getPersonelKodu() {
        return personelKodu;
    }

    public void setPersonelKodu(String personelKodu) {
        this.personelKodu = personelKodu;
    }

    public String getAciklama() {
        return aciklama;
    }

    public void setAciklama(String aciklama) {
        this.aciklama = aciklama;
    }

    public String getIslem() {
        return islem;
    }

    public void setIslem(String islem) {
        this.islem = islem;
    }

    public String getSarfMalzemeGuid() {
        return sarfMalzemeGuid;
    }

    public void setSarfMalzemeGuid(String sarfMalzemeGuid) {
        this.sarfMalzemeGuid = sarfMalzemeGuid;
    }

    public Depo getDepo() {
        return depo;
    }

    public void setDepo(Depo depo) {
        this.depo = depo;
    }
}
