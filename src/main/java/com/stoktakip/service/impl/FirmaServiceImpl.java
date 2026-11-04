package com.stoktakip.service.impl;

import com.stoktakip.dto.FirmaCreateRequest;
import com.stoktakip.dto.FirmaResponse;
import com.stoktakip.dto.FirmaUpdateRequest;
import com.stoktakip.entity.Firma;
import com.stoktakip.enums.EnvanterTuru;
import com.stoktakip.exception.BusinessRuleException;
import com.stoktakip.exception.ResourceNotFoundException;
import com.stoktakip.repository.FirmaRepository;
import com.stoktakip.service.Guidler;
import com.stoktakip.service.FirmaService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FirmaServiceImpl implements FirmaService {
    private final FirmaRepository repository;

    public FirmaServiceImpl(FirmaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<FirmaResponse> getAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public FirmaResponse getById(String guid) {
        return toResponse(find(guid));
    }

    @Override
    @Transactional
    public FirmaResponse create(FirmaCreateRequest request) {
        if (request == null) throw new BusinessRuleException("Firma oluşturma talebi zorunludur");
        Firma firma = new Firma();
        firma.setGuid(UUID.randomUUID().toString());
        firma.setOlusturmaTarihi(LocalDateTime.now());
        apply(firma, request.ad(), request.yetkiliKisi(), request.yetkiliTelefon(),
            request.irtibatKisi(), request.irtibatTelefon(), request.telefon1(), request.telefon2(),
            request.faks(), request.webAdresi(), request.adres(), request.aciklama(),
            request.durum(), request.kaynakAlani());
        return toResponse(repository.save(firma));
    }

    @Override
    @Transactional
    public FirmaResponse update(String guid, FirmaUpdateRequest request) {
        if (request == null) throw new BusinessRuleException("Firma güncelleme talebi zorunludur");
        Firma firma = find(guid);
        apply(firma, request.ad(), request.yetkiliKisi(), request.yetkiliTelefon(),
            request.irtibatKisi(), request.irtibatTelefon(), request.telefon1(), request.telefon2(),
            request.faks(), request.webAdresi(), request.adres(), request.aciklama(),
            request.durum(), request.kaynakAlani());
        return toResponse(repository.save(firma));
    }

    @Override
    @Transactional
    public void delete(String guid) {
        Firma firma = find(guid);
        try {
            repository.delete(firma);
            repository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessRuleException("Firma kullanımda olduğu için silinemez");
        }
    }

    private Firma find(String guid) {
        String id = Guidler.zorunlu(guid, "firmaGuid");
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Firma bulunamadı: " + id));
    }

    private static void apply(
        Firma firma, String ad, String yetkiliKisi, String yetkiliTelefon,
        String irtibatKisi, String irtibatTelefon, String telefon1, String telefon2,
        String faks, String webAdresi, String adres, String aciklama, String durum,
        EnvanterTuru kaynakAlani
    ) {
        if (ad == null || ad.isBlank()) throw new BusinessRuleException("ad zorunludur");
        if (kaynakAlani == null) throw new BusinessRuleException("kaynakAlani zorunludur");
        firma.setAd(ad.trim());
        firma.setYetkiliKisi(yetkiliKisi);
        firma.setYetkiliTelefon(yetkiliTelefon);
        firma.setIrtibatKisi(irtibatKisi);
        firma.setIrtibatTelefon(irtibatTelefon);
        firma.setTelefon1(telefon1);
        firma.setTelefon2(telefon2);
        firma.setFaks(faks);
        firma.setWebAdresi(webAdresi);
        firma.setAdres(adres);
        firma.setAciklama(aciklama);
        firma.setDurum(durum);
        firma.setKaynakAlani(kaynakAlani);
    }

    private FirmaResponse toResponse(Firma firma) {
        return new FirmaResponse(firma.getGuid(), firma.getAd(), firma.getYetkiliKisi(),
            firma.getYetkiliTelefon(), firma.getIrtibatKisi(), firma.getIrtibatTelefon(),
            firma.getTelefon1(), firma.getTelefon2(), firma.getFaks(), firma.getWebAdresi(),
            firma.getAdres(), firma.getAciklama(), firma.getDurum(), firma.getKaynakAlani(),
            firma.getOlusturmaTarihi());
    }
}
