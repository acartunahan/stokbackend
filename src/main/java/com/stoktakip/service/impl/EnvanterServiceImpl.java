package com.stoktakip.service.impl;

import com.stoktakip.dto.EnvanterResponse;
import com.stoktakip.entity.Envanter;
import com.stoktakip.enums.EnvanterTuru;
import com.stoktakip.repository.EnvanterRepository;
import com.stoktakip.service.EnvanterService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EnvanterServiceImpl implements EnvanterService {
    private final EnvanterRepository repository;

    public EnvanterServiceImpl(EnvanterRepository repository) {
        this.repository = repository;
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

    private List<EnvanterResponse> getByType(EnvanterTuru type) {
        return repository.findByEnvanterTuru(type).stream().map(this::toResponse).toList();
    }

    private EnvanterResponse toResponse(Envanter entity) {
        String depoGuid = entity.getDepo() == null ? null : entity.getDepo().getGuid();
        return new EnvanterResponse(entity.getGuid(), entity.getEnvanterAdi(),
            entity.getEnvanterTuru(), depoGuid);
    }
}
