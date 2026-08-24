package com.stoktakip.entity;

import com.stoktakip.enums.ZimmetHareketTipi;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "PERSONEL_ZIMMET_HAREKET")
public class PersonelZimmetHareket extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "personel_zimmet_hareket_seq")
    @SequenceGenerator(
        name = "personel_zimmet_hareket_seq",
        sequenceName = "PERSONEL_ZIMMET_HAREKET_SEQ",
        allocationSize = 1
    )
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ENVANTER_GUID", referencedColumnName = "GUID")
    private Envanter envanter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APARAT_GUID", referencedColumnName = "GUID")
    private Aparat aparat;

    @Column(name = "PERSONEL_KODU", length = 100)
    private String personelKodu;

    @Enumerated(EnumType.STRING)
    @Column(name = "HAREKET_TIPI", length = 32)
    private ZimmetHareketTipi hareketTipi;

    @Column(name = "ZIMMET_TURU", length = 100)
    private String zimmetTuru;

    @Column(name = "ACIKLAMA", length = 1000)
    private String aciklama;

    @Column(name = "ISLEM_YAPAN_PERSONEL", length = 100)
    private String islemYapanPersonel;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Envanter getEnvanter() {
        return envanter;
    }

    public void setEnvanter(Envanter envanter) {
        this.envanter = envanter;
    }

    public Aparat getAparat() {
        return aparat;
    }

    public void setAparat(Aparat aparat) {
        this.aparat = aparat;
    }

    public String getPersonelKodu() { return personelKodu; }
    public void setPersonelKodu(String personelKodu) { this.personelKodu = personelKodu; }
    public ZimmetHareketTipi getHareketTipi() { return hareketTipi; }
    public void setHareketTipi(ZimmetHareketTipi hareketTipi) { this.hareketTipi = hareketTipi; }

    public String getZimmetTuru() {
        return zimmetTuru;
    }

    public void setZimmetTuru(String zimmetTuru) {
        this.zimmetTuru = zimmetTuru;
    }

    public String getAciklama() {
        return aciklama;
    }

    public void setAciklama(String aciklama) {
        this.aciklama = aciklama;
    }

    public String getIslemYapanPersonel() {
        return islemYapanPersonel;
    }

    public void setIslemYapanPersonel(String islemYapanPersonel) {
        this.islemYapanPersonel = islemYapanPersonel;
    }

    @PrePersist
    @PreUpdate
    private void varlikIliskisiniDogrula() {
        if ((envanter == null) == (aparat == null)) {
            throw new IllegalStateException("Personel zimmet hareketi tam olarak bir varliga bagli olmalidir");
        }
    }
}
