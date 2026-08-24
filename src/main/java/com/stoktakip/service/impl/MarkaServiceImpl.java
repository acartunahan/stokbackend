package com.stoktakip.service.impl;

import com.stoktakip.dto.MarkaCreateRequest;
import com.stoktakip.dto.MarkaResponse;
import com.stoktakip.dto.MarkaUpdateRequest;
import com.stoktakip.entity.Marka;
import com.stoktakip.exception.BusinessRuleException;
import com.stoktakip.exception.ResourceNotFoundException;
import com.stoktakip.repository.MarkaRepository;
import com.stoktakip.service.MarkaService;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MarkaServiceImpl implements MarkaService {
    private final MarkaRepository repository;

    public MarkaServiceImpl(MarkaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<MarkaResponse> getAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public List<MarkaResponse> searchByName(String adi) {
        String aramaMetni = normalizeSearchTerm(adi);
        return repository.findByMarkaAdiContainingIgnoreCase(aramaMetni).stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    @Transactional
    public MarkaResponse create(MarkaCreateRequest request) {
        Marka entity = new Marka();
        entity.setGuid(UUID.randomUUID().toString());
        entity.setMarkaAdi(request.markaAdi().trim());
        entity.setAciklama(request.aciklama());
        return toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public MarkaResponse update(String guid, MarkaUpdateRequest request) {
        Marka entity = find(guid);
        entity.setMarkaAdi(request.markaAdi().trim());
        entity.setAciklama(request.aciklama());
        return toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(String guid) {
        Marka entity = find(guid);
        try {
            repository.delete(entity);
            repository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessRuleException("Marka kullanımda olduğu için silinemez");
        }
    }

    private Marka find(String guid) {
        return repository.findById(guid)
            .orElseThrow(() -> new ResourceNotFoundException("Marka bulunamadı: " + guid));
    }

    private static String normalizeSearchTerm(String adi) {
        if (adi == null || adi.isBlank()) {
            throw new BusinessRuleException("adi boş olamaz");
        }
        return adi.trim();
    }

    private MarkaResponse toResponse(Marka entity) {
        return new MarkaResponse(entity.getGuid(), entity.getMarkaAdi(), entity.getAciklama(),
            entity.getAktif(), entity.getOlusturmaTarihi(), entity.getGuncellemeTarihi());
    }
}
