package com.stoktakip.service.impl;

import com.stoktakip.dto.KategoriRequest;
import com.stoktakip.dto.KategoriResponse;
import com.stoktakip.dto.KategoriTreeResponse;
import com.stoktakip.entity.Kategori;
import com.stoktakip.exception.BusinessRuleException;
import com.stoktakip.exception.ResourceNotFoundException;
import com.stoktakip.repository.KategoriRepository;
import com.stoktakip.service.KategoriService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class KategoriServiceImpl implements KategoriService {

    private final KategoriRepository repository;

    public KategoriServiceImpl(KategoriRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<KategoriResponse> getAll() {
        return repository.findAll().stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    public KategoriResponse getById(String guid) {
        return toResponse(find(guid));
    }

    @Override
    @Transactional
    public KategoriResponse create(KategoriRequest request) {
        Kategori kategori = new Kategori();
        kategori.setGuid(UUID.randomUUID().toString());
        kategori.setBaslik(request.baslik().trim());
        kategori.setParent(resolveParent(request.parentId()));
        return toResponse(repository.save(kategori));
    }

    @Override
    @Transactional
    public KategoriResponse update(String guid, KategoriRequest request) {
        String normalizedGuid = normalizeGuid(guid);
        Kategori kategori = find(normalizedGuid);
        Kategori parent = resolveParent(request.parentId());

        if (parent != null) {
            if (normalizedGuid.equals(parent.getGuid())) {
                throw new BusinessRuleException("Kategori kendi üst kategorisi olamaz");
            }
            validateNoCycle(kategori, parent);
        }

        kategori.setBaslik(request.baslik().trim());
        kategori.setParent(parent);
        return toResponse(repository.save(kategori));
    }

    @Override
    @Transactional
    public void delete(String guid) {
        Kategori kategori = find(guid);
        if (kategori.getAltKategoriler().stream().anyMatch(child -> !Boolean.TRUE.equals(child.getSilindi()))) {
            throw new BusinessRuleException("Alt kategorisi bulunan kategori silinemez");
        }
        if (kategori.getModeller().stream().anyMatch(model -> !Boolean.TRUE.equals(model.getSilindi()))) {
            throw new BusinessRuleException("Model atanmış kategori silinemez");
        }

        try {
            repository.delete(kategori);
            repository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessRuleException("Kategori kullanımda olduğu için silinemez");
        }
    }

    @Override
    public List<KategoriTreeResponse> getCategoryTree() {
        Map<String, List<Kategori>> altKategoriler = new HashMap<>();
        List<Kategori> kokler = new ArrayList<>();
        for (Kategori kategori : repository.findAll(Sort.by("baslik"))) {
            Kategori parent = kategori.getParent();
            if (parent == null) {
                kokler.add(kategori);
            } else {
                altKategoriler.computeIfAbsent(parent.getGuid(), key -> new ArrayList<>()).add(kategori);
            }
        }
        return kokler.stream()
            .map(root -> toTreeResponse(root, altKategoriler))
            .toList();
    }

    private Kategori find(String guid) {
        String normalizedGuid = normalizeGuid(guid);
        return repository.findById(normalizedGuid)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Kategori bulunamadı: " + normalizedGuid));
    }

    private Kategori resolveParent(String parentId) {
        if (parentId == null || parentId.isBlank()) {
            return null;
        }
        return find(parentId);
    }

    private static void validateNoCycle(Kategori kategori, Kategori parent) {
        Set<String> visited = new HashSet<>();
        Kategori current = parent;
        while (current != null) {
            if (!visited.add(current.getGuid())) {
                throw new BusinessRuleException("Kategori ağacında döngü tespit edildi");
            }
            if (kategori.getGuid().equals(current.getGuid())) {
                throw new BusinessRuleException("Kategori kendi alt kategorisine bağlanamaz");
            }
            current = current.getParent();
        }
    }

    // Ağaç kökten aşağı kurulduğu için döngüdeki kayıtlar hiçbir köke bağlanmaz ve sonsuz döngü oluşmaz.
    private static KategoriTreeResponse toTreeResponse(
        Kategori kategori, Map<String, List<Kategori>> altKategoriler
    ) {
        List<KategoriTreeResponse> children = altKategoriler.getOrDefault(kategori.getGuid(), List.of()).stream()
            .map(child -> toTreeResponse(child, altKategoriler))
            .toList();
        return new KategoriTreeResponse(kategori.getGuid(), kategori.getBaslik(), children);
    }

    private KategoriResponse toResponse(Kategori kategori) {
        Kategori parent = kategori.getParent();
        return new KategoriResponse(
            kategori.getGuid(),
            kategori.getBaslik(),
            parent == null ? null : parent.getGuid(),
            parent == null ? null : parent.getBaslik(),
            kategori.getAktif(),
            kategori.getOlusturmaTarihi(),
            kategori.getGuncellemeTarihi()
        );
    }

    private static String normalizeGuid(String guid) {
        if (guid == null || guid.isBlank()) {
            throw new BusinessRuleException("kategoriGuid zorunludur");
        }
        return guid.trim();
    }
}
