package com.stoktakip.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record TopluDepoTransferRequest(
    @NotBlank String kaynakDepoGuid,
    @NotBlank String hedefDepoGuid,
    @NotEmpty List<@Valid TopluEnvanterItemRequest> envanterler,
    @Size(max = 1000) String aciklama,
    @Size(max = 100) String islemYapanPersonel
) {
}
