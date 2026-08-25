package com.stoktakip.service.impl;

import com.stoktakip.dto.DepoCreateRequest;
import com.stoktakip.dto.DepoResponse;
import com.stoktakip.dto.DepoUpdateRequest;
import com.stoktakip.entity.Depo;
import com.stoktakip.exception.BusinessRuleException;
import com.stoktakip.exception.ResourceNotFoundException;
import com.stoktakip.repository.DepoRepository;
import com.stoktakip.service.DepoService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DepoServiceImpl implements DepoService {
    private final DepoRepository repository;

    public DepoServiceImpl(DepoRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<DepoResponse> getAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public DepoResponse getById(String guid) {
        return toResponse(find(guid));
    }

    @Override
    @Transactional
    public DepoResponse create(DepoCreateRequest request) {
        if (request == null) throw new BusinessRuleException("Depo oluşturma talebi zorunludur");
        Depo depo = new Depo();
        depo.setGuid(UUID.randomUUID().toString());
        depo.setOlusturmaTarihi(LocalDateTime.now());
        apply(depo, request.ad(), request.aciklama(), request.personelKodu(), request.durum());
        return toResponse(repository.save(depo));
    }

    @Override
    @Transactional
    public DepoResponse update(String guid, DepoUpdateRequest request) {
        if (request == null) throw new BusinessRuleException("Depo güncelleme talebi zorunludur");
        Depo depo = find(guid);
        apply(depo, request.ad(), request.aciklama(), request.personelKodu(), request.durum());
        return toResponse(repository.save(depo));
    }

    @Override
    @Transactional
    public void delete(String guid) {
        Depo depo = find(guid);
        try {
            repository.delete(depo);
            repository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessRuleException("Depo kullanımda olduğu için silinemez");
        }
    }

    private Depo find(String guid) {
        String id = id(guid, "depoGuid");
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Depo bulunamadı: " + id));
    }

    private static void apply(Depo depo, String ad, String aciklama, String personelKodu, String durum) {
        if (ad == null || ad.isBlank()) throw new BusinessRuleException("ad zorunludur");
        depo.setAd(ad.trim());
        depo.setAciklama(aciklama);
        depo.setPersonelKodu(personelKodu);
        depo.setDurum(durum);
    }

    private DepoResponse toResponse(Depo depo) {
        return new DepoResponse(depo.getGuid(), depo.getAd(), depo.getAciklama(),
            depo.getPersonelKodu(), depo.getDurum(), depo.getOlusturmaTarihi());
    }

    private static String id(String value, String field) {
        if (value == null || value.isBlank()) throw new BusinessRuleException(field + " zorunludur");
        return value.trim();
    }
}
