package com.stoktakip.service.impl;

import com.stoktakip.dto.IhaleCreateRequest;
import com.stoktakip.dto.IhaleResponse;
import com.stoktakip.dto.IhaleUpdateRequest;
import com.stoktakip.entity.Firma;
import com.stoktakip.entity.Ihale;
import com.stoktakip.exception.BusinessRuleException;
import com.stoktakip.exception.ResourceNotFoundException;
import com.stoktakip.repository.FirmaRepository;
import com.stoktakip.repository.IhaleRepository;
import com.stoktakip.service.IhaleService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class IhaleServiceImpl implements IhaleService {
    private final IhaleRepository repository;
    private final FirmaRepository firmaRepository;

    public IhaleServiceImpl(IhaleRepository repository, FirmaRepository firmaRepository) {
        this.repository = repository;
        this.firmaRepository = firmaRepository;
    }

    @Override
    public List<IhaleResponse> getAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public IhaleResponse getById(String guid) {
        return toResponse(find(guid));
    }

    @Override
    @Transactional
    public IhaleResponse create(IhaleCreateRequest request) {
        if (request == null) throw new BusinessRuleException("İhale oluşturma talebi zorunludur");
        Firma firma = firma(request.firmaGuid());
        Ihale ihale = new Ihale();
        ihale.setGuid(UUID.randomUUID().toString());
        ihale.setOlusturmaTarihi(LocalDateTime.now());
        apply(ihale, firma, request.ad(), request.ihaleTarihi(), request.kabulTarihi(),
            request.sozlesmeTarihi(), request.birimKodu(), request.personel(), request.aciklama(),
            request.komisyonBaskani(), request.uye1(), request.uye2(), request.yerindeGaranti(),
            request.onarimSuresi(), request.durum());
        return toResponse(repository.save(ihale));
    }

    @Override
    @Transactional
    public IhaleResponse update(String guid, IhaleUpdateRequest request) {
        if (request == null) throw new BusinessRuleException("İhale güncelleme talebi zorunludur");
        Ihale ihale = find(guid);
        Firma firma = firma(request.firmaGuid());
        apply(ihale, firma, request.ad(), request.ihaleTarihi(), request.kabulTarihi(),
            request.sozlesmeTarihi(), request.birimKodu(), request.personel(), request.aciklama(),
            request.komisyonBaskani(), request.uye1(), request.uye2(), request.yerindeGaranti(),
            request.onarimSuresi(), request.durum());
        return toResponse(repository.save(ihale));
    }

    @Override
    @Transactional
    public void delete(String guid) {
        Ihale ihale = find(guid);
        try {
            repository.delete(ihale);
            repository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessRuleException("İhale kullanımda olduğu için silinemez");
        }
    }

    private Ihale find(String guid) {
        String id = id(guid, "ihaleGuid");
        return repository.findDetayByGuid(id)
            .orElseThrow(() -> new ResourceNotFoundException("İhale bulunamadı: " + id));
    }

    private Firma firma(String guid) {
        String id = id(guid, "firmaGuid");
        return firmaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Firma bulunamadı: " + id));
    }

    private static void apply(
        Ihale ihale, Firma firma, String ad, LocalDateTime ihaleTarihi,
        LocalDateTime kabulTarihi, LocalDateTime sozlesmeTarihi, Long birimKodu,
        String personel, String aciklama, String komisyonBaskani, String uye1,
        String uye2, Long yerindeGaranti, Long onarimSuresi, String durum
    ) {
        if (ad == null || ad.isBlank()) throw new BusinessRuleException("ad zorunludur");
        ihale.setFirma(firma);
        ihale.setAd(ad.trim());
        ihale.setIhaleTarihi(ihaleTarihi);
        ihale.setKabulTarihi(kabulTarihi);
        ihale.setSozlesmeTarihi(sozlesmeTarihi);
        ihale.setBirimKodu(birimKodu);
        ihale.setPersonel(personel);
        ihale.setAciklama(aciklama);
        ihale.setKomisyonBaskani(komisyonBaskani);
        ihale.setUye1(uye1);
        ihale.setUye2(uye2);
        ihale.setYerindeGaranti(yerindeGaranti);
        ihale.setOnarimSuresi(onarimSuresi);
        ihale.setDurum(durum);
    }

    private IhaleResponse toResponse(Ihale ihale) {
        Firma firma = ihale.getFirma();
        return new IhaleResponse(ihale.getGuid(), firma.getGuid(), firma.getAd(), ihale.getAd(),
            ihale.getIhaleTarihi(), ihale.getKabulTarihi(), ihale.getSozlesmeTarihi(),
            ihale.getBirimKodu(), ihale.getPersonel(), ihale.getAciklama(),
            ihale.getKomisyonBaskani(), ihale.getUye1(), ihale.getUye2(),
            ihale.getYerindeGaranti(), ihale.getOnarimSuresi(), ihale.getDurum(),
            ihale.getOlusturmaTarihi());
    }

    private static String id(String value, String field) {
        if (value == null || value.isBlank()) throw new BusinessRuleException(field + " zorunludur");
        return value.trim();
    }
}
