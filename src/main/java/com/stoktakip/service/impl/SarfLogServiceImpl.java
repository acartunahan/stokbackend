package com.stoktakip.service.impl;

import com.stoktakip.dto.PageResponse;
import com.stoktakip.dto.SarfLogResponse;
import com.stoktakip.entity.SarfLogKaydi;
import com.stoktakip.repository.SarfLogKaydiRepository;
import com.stoktakip.service.PageableFactory;
import com.stoktakip.service.SarfLogService;
import java.time.LocalDateTime;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SarfLogServiceImpl implements SarfLogService {
    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "olusturmaTarihi");
    private static final Set<String> SORT_FIELDS = Set.of("olusturmaTarihi", "personelKodu", "islem");

    private final SarfLogKaydiRepository repository;

    public SarfLogServiceImpl(SarfLogKaydiRepository repository) {
        this.repository = repository;
    }

    @Override
    public PageResponse<SarfLogResponse> search(
        String depoGuid, String personelKodu, String sarfMalzemeGuid,
        LocalDateTime baslangicTarihi, LocalDateTime bitisTarihi, Pageable pageable
    ) {
        Pageable safe = PageableFactory.sanitize(pageable, DEFAULT_SORT, SORT_FIELDS);
        return PageResponse.from(repository.search(blankToNull(depoGuid), blankToNull(personelKodu),
            blankToNull(sarfMalzemeGuid), baslangicTarihi, bitisTarihi, safe).map(this::toResponse));
    }

    private SarfLogResponse toResponse(SarfLogKaydi kayit) {
        return new SarfLogResponse(kayit.getGuid(), kayit.getPersonelKodu(), kayit.getIslem(),
            kayit.getAciklama(), kayit.getSarfMalzemeGuid(),
            kayit.getDepo() == null ? null : kayit.getDepo().getGuid(),
            kayit.getDepo() == null ? null : kayit.getDepo().getAd(), kayit.getOlusturmaTarihi());
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
