package com.stoktakip.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

@Entity
@Table(name = "KATEGORI")
public class Kategori extends BaseEntity{

    @Id
    @Column(name = "GUID", length = 128, nullable = false)
    private String guid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT", referencedColumnName = "GUID", nullable = true)
    private Kategori parent;

    @Column(name = "BASLIK", length = 120, nullable = false)
    private String baslik;

    @OneToMany(mappedBy = "kategori", fetch = FetchType.LAZY)
    private List<Model> modeller = new ArrayList<>();

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private List<Kategori> altKategoriler = new ArrayList<>();

    public String getGuid() {
        return guid;
    }
}
