package com.stoktakip.service.impl;

import com.stoktakip.dto.EnvanterAciklamaCreateRequest;
import com.stoktakip.dto.EnvanterAciklamaResponse;
import com.stoktakip.entity.Envanter;
import com.stoktakip.entity.EnvanterAciklama;
import com.stoktakip.exception.BusinessRuleException;
import com.stoktakip.exception.ResourceNotFoundException;
import com.stoktakip.repository.EnvanterAciklamaRepository;
import com.stoktakip.repository.EnvanterRepository;
import com.stoktakip.service.Guidler;
import com.stoktakip.service.EnvanterAciklamaService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EnvanterAciklamaServiceImpl implements EnvanterAciklamaService {
    private final EnvanterAciklamaRepository repository;
    private final EnvanterRepository envanterRepository;

    public EnvanterAciklamaServiceImpl(
        EnvanterAciklamaRepository repository, EnvanterRepository envanterRepository
    ) {
        this.repository = repository;
        this.envanterRepository = envanterRepository;
    }

    @Override
    public List<EnvanterAciklamaResponse> getByEnvanter(String envanterGuid) {
        Envanter envanter = findEnvanter(envanterGuid);
        return repository.findAktifByEnvanterGuid(envanter.getGuid()).stream()
            .map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public EnvanterAciklamaResponse create(String envanterGuid, EnvanterAciklamaCreateRequest request) {
        if (request == null) throw new BusinessRuleException("Açıklama talebi zorunludur");
        Envanter envanter = findEnvanter(envanterGuid);
        EnvanterAciklama aciklama = new EnvanterAciklama();
        aciklama.setGuid(UUID.randomUUID().toString());
        aciklama.setEnvanter(envanter);
        aciklama.setNotMetni(request.notMetni().trim());
        aciklama.setZimmetliPersonelMetni(request.zimmetliPersonelMetni());
        aciklama.setEkleyen(request.ekleyen());
        aciklama.setEkleyenPersonel(request.ekleyenPersonel());
        aciklama.setDurum("AKTIF");
        aciklama.setOlusturmaTarihi(LocalDateTime.now());
        return toResponse(repository.save(aciklama));
    }

    @Override
    @Transactional
    public void delete(String envanterGuid, String aciklamaGuid) {
        Envanter envanter = findEnvanter(envanterGuid);
        String id = Guidler.zorunlu(aciklamaGuid, "aciklamaGuid");
        EnvanterAciklama aciklama = repository.findById(id)
            .filter(item -> item.getEnvanter() != null
                && envanter.getGuid().equals(item.getEnvanter().getGuid()))
            .orElseThrow(() -> new ResourceNotFoundException("Açıklama bulunamadı: " + id));
        repository.delete(aciklama);
    }

    private Envanter findEnvanter(String guid) {
        String id = Guidler.zorunlu(guid, "envanterGuid");
        return envanterRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Envanter bulunamadı: " + id));
    }

    private EnvanterAciklamaResponse toResponse(EnvanterAciklama aciklama) {
        return new EnvanterAciklamaResponse(aciklama.getGuid(),
            aciklama.getEnvanter() == null ? null : aciklama.getEnvanter().getGuid(),
            aciklama.getNotMetni(), aciklama.getZimmetliPersonelMetni(), aciklama.getEkleyen(),
            aciklama.getEkleyenPersonel(), aciklama.getOlusturmaTarihi());
    }
}
