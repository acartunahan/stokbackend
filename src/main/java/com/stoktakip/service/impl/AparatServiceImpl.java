package com.stoktakip.service.impl;

import com.stoktakip.dto.AparatCreateRequest;
import com.stoktakip.dto.AparatResponse;
import com.stoktakip.entity.Aparat;
import com.stoktakip.entity.Envanter;
import com.stoktakip.exception.BusinessRuleException;
import com.stoktakip.exception.ResourceNotFoundException;
import com.stoktakip.repository.AparatRepository;
import com.stoktakip.repository.EnvanterRepository;
import com.stoktakip.service.AparatService;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AparatServiceImpl implements AparatService {
    private final AparatRepository repository;
    private final EnvanterRepository envanterRepository;

    public AparatServiceImpl(AparatRepository repository, EnvanterRepository envanterRepository) {
        this.repository = repository;
        this.envanterRepository = envanterRepository;
    }

    @Override
    public List<AparatResponse> getAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public AparatResponse getById(String guid) {
        return toResponse(find(guid));
    }

    @Override
    @Transactional
    public AparatResponse create(AparatCreateRequest request) {
        Envanter envanter = envanterRepository.findById(request.envanterGuid())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Envanter bulunamadı: " + request.envanterGuid()));
        Aparat entity = new Aparat();
        entity.setGuid(UUID.randomUUID().toString());
        entity.setEnvanter(envanter);
        entity.setAparatAdi(request.aparatAdi().trim());
        entity.setAciklama(request.aciklama());
        return toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(String guid) {
        Aparat entity = find(guid);
        try {
            repository.delete(entity);
            repository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessRuleException("Aparat kullanımda olduğu için silinemez");
        }
    }

    private Aparat find(String guid) {
        return repository.findById(guid)
            .orElseThrow(() -> new ResourceNotFoundException("Aparat bulunamadı: " + guid));
    }

    private AparatResponse toResponse(Aparat entity) {
        return new AparatResponse(entity.getGuid(), entity.getEnvanter().getGuid(),
            entity.getAparatAdi(), entity.getAciklama(), entity.getAktif());
    }
}
