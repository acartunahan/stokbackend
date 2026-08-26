package com.stoktakip.service.impl;

import com.stoktakip.dto.ModelCreateRequest;
import com.stoktakip.dto.ModelResponse;
import com.stoktakip.dto.ModelUpdateRequest;
import com.stoktakip.entity.Kategori;
import com.stoktakip.entity.Marka;
import com.stoktakip.entity.Model;
import com.stoktakip.exception.BusinessRuleException;
import com.stoktakip.exception.ResourceNotFoundException;
import com.stoktakip.repository.KategoriRepository;
import com.stoktakip.repository.MarkaRepository;
import com.stoktakip.repository.ModelRepository;
import com.stoktakip.service.ModelService;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ModelServiceImpl implements ModelService {
    private final ModelRepository repository;
    private final MarkaRepository markaRepository;
    private final KategoriRepository kategoriRepository;

    public ModelServiceImpl(ModelRepository repository, MarkaRepository markaRepository,
                            KategoriRepository kategoriRepository) {
        this.repository = repository;
        this.markaRepository = markaRepository;
        this.kategoriRepository = kategoriRepository;
    }

    @Override
    public List<ModelResponse> getAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public List<ModelResponse> getByMarkaGuid(String markaGuid) {
        String guid = markaGuid(markaGuid);
        findMarka(guid);
        return repository.findByMarka_Guid(guid).stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    public List<ModelResponse> searchByMarkaAndName(String markaGuid, String adi) {
        String guid = markaGuid(markaGuid);
        String aramaMetni = normalizeSearchTerm(adi);
        findMarka(guid);
        return repository.findByMarka_GuidAndModelAdiContainingIgnoreCaseAndSilindiFalse(guid, aramaMetni).stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    @Transactional
    public ModelResponse create(ModelCreateRequest request) {
        Model entity = new Model();
        entity.setGuid(UUID.randomUUID().toString());
        entity.setModelAdi(request.modelAdi().trim());
        entity.setAciklama(request.aciklama());
        entity.setMarka(findMarka(markaGuid(request.markaGuid())));
        entity.setKategori(resolveKategori(request.kategoriGuid()));
        return toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public ModelResponse update(String guid, ModelUpdateRequest request) {
        Model entity = find(guid);
        entity.setModelAdi(request.modelAdi().trim());
        entity.setAciklama(request.aciklama());
        entity.setMarka(findMarka(markaGuid(request.markaGuid())));
        return toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(String guid) {
        Model entity = find(guid);
        try {
            repository.delete(entity);
            repository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessRuleException("Model kullanımda olduğu için silinemez");
        }
    }

    private Model find(String guid) {
        return repository.findById(guid)
            .orElseThrow(() -> new ResourceNotFoundException("Model bulunamadı: " + guid));
    }

    private Marka findMarka(String guid) {
        return markaRepository.findById(guid)
            .orElseThrow(() -> new ResourceNotFoundException("Marka bulunamadı: " + guid));
    }

    private Kategori resolveKategori(String guid) {
        if (guid == null || guid.isBlank()) return null;
        return kategoriRepository.findById(guid)
            .orElseThrow(() -> new ResourceNotFoundException("Kategori bulunamadı: " + guid));
    }

    private static String normalizeSearchTerm(String adi) {
        if (adi == null || adi.isBlank()) {
            throw new BusinessRuleException("adi boş olamaz");
        }
        return adi.trim();
    }

    private static String markaGuid(String markaGuid) {
        if (markaGuid == null || markaGuid.isBlank()) {
            throw new BusinessRuleException("markaGuid boş olamaz");
        }
        return markaGuid.trim();
    }

    private ModelResponse toResponse(Model entity) {
        String markaGuid = entity.getMarka() == null ? null : entity.getMarka().getGuid();
        String markaAdi = entity.getMarka() == null ? null : entity.getMarka().getMarkaAdi();
        String kategoriGuid = entity.getKategori() == null ? null : entity.getKategori().getGuid();
        return new ModelResponse(entity.getGuid(), entity.getModelAdi(), entity.getAciklama(),
            markaGuid, markaAdi, kategoriGuid, entity.getAktif());
    }
}
