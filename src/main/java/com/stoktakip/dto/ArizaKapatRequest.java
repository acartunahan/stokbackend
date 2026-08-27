package com.stoktakip.dto;

import jakarta.validation.constraints.Size;

public record ArizaKapatRequest(@Size(max = 2000) String aciklama) {}
