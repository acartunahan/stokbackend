package com.stoktakip.service;

import com.stoktakip.dto.GarantiBitenResponse;
import com.stoktakip.dto.RaporOzetResponse;
import java.util.List;

public interface RaporService {
    RaporOzetResponse getOzet();
    List<GarantiBitenResponse> getGarantisiBitecekler(int gun);
}
