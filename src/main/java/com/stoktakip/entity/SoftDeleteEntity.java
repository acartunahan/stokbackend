package com.stoktakip.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;

@MappedSuperclass
public abstract class SoftDeleteEntity {

    @Column(name = "SILINDI", nullable = false)
    private Boolean silindi = Boolean.FALSE;

    @PrePersist
    protected void softDeleteDefault() {
        if (silindi == null) {
            silindi = Boolean.FALSE;
        }
    }

    public Boolean getSilindi() {
        return silindi;
    }

    public void setSilindi(Boolean silindi) {
        this.silindi = Boolean.TRUE.equals(silindi);
    }

    public void softDelete() {
        this.silindi = Boolean.TRUE;
    }
}
