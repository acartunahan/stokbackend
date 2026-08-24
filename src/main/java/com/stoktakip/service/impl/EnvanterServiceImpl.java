package com.stoktakip.service.impl;

import com.stoktakip.dto.EnvanterResponse;
import com.stoktakip.dto.EnvanterZimmetDurumuResponse;
import com.stoktakip.entity.Envanter;
import com.stoktakip.entity.PersonelZimmetHareket;
import com.stoktakip.enums.EnvanterTuru;
import com.stoktakip.enums.ZimmetHareketTipi;
import com.stoktakip.exception.BusinessRuleException;
import com.stoktakip.exception.ResourceNotFoundException;
import com.stoktakip.repository.EnvanterRepository;
import com.stoktakip.repository.PersonelZimmetHareketRepository;
import com.stoktakip.service.EnvanterService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EnvanterServiceImpl implements EnvanterService {
    private final EnvanterRepository repository;
    private final PersonelZimmetHareketRepository hareketRepository;

    public EnvanterServiceImpl(
        EnvanterRepository repository,
        PersonelZimmetHareketRepository hareketRepository
    ) {
        this.repository = repository;
        this.hareketRepository = hareketRepository;
    }

    @Override
    public List<EnvanterResponse> getAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public List<EnvanterResponse> getSarflar() {
        return getByType(EnvanterTuru.SARF);
    }

    @Override
    public List<EnvanterResponse> getDemirbaslar() {
        return getByType(EnvanterTuru.DEMIRBAS);
    }

    @Override
    public EnvanterZimmetDurumuResponse getZimmetDurumu(String envanterGuid) {
        String guid = normalizeGuid(envanterGuid);
        Envanter envanter = repository.findById(guid)
            .orElseThrow(() -> new ResourceNotFoundException("Envanter bulunamadı: " + guid));

        return hareketRepository.findFirstByEnvanterGuidOrderByIdDesc(guid)
            .filter(this::aktifPersonelZimmeti)
            .map(hareket -> zimmetliResponse(envanter, hareket))
            .orElseGet(() -> zimmetsizResponse(envanter));
    }

    private List<EnvanterResponse> getByType(EnvanterTuru type) {
        return repository.findByEnvanterTuru(type).stream().map(this::toResponse).toList();
    }

    private EnvanterResponse toResponse(Envanter entity) {
        String depoGuid = entity.getDepo() == null ? null : entity.getDepo().getGuid();
        return new EnvanterResponse(entity.getGuid(), entity.getEnvanterAdi(),
            entity.getEnvanterTuru(), depoGuid);
    }

    private boolean aktifPersonelZimmeti(PersonelZimmetHareket hareket) {
        return hareket.getHareketTipi() == ZimmetHareketTipi.PERSONELE_ZIMMET
            && Boolean.TRUE.equals(hareket.getAktif());
    }

    private EnvanterZimmetDurumuResponse zimmetliResponse(
        Envanter envanter,
        PersonelZimmetHareket hareket
    ) {
        return new EnvanterZimmetDurumuResponse(
            envanter.getGuid(), envanter.getEnvanterAdi(), true,
            hareket.getPersonelKodu(), hareket.getId(), hareket.getOlusturmaTarihi());
    }

    private EnvanterZimmetDurumuResponse zimmetsizResponse(Envanter envanter) {
        return new EnvanterZimmetDurumuResponse(
            envanter.getGuid(), envanter.getEnvanterAdi(), false, null, null, null);
    }

    private static String normalizeGuid(String envanterGuid) {
        if (envanterGuid == null || envanterGuid.isBlank()) {
            throw new BusinessRuleException("envanterGuid boş olamaz");
        }
        return envanterGuid.trim();
    }
}
