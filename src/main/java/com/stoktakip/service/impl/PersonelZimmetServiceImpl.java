package com.stoktakip.service.impl;

import com.stoktakip.dto.PersonelEnvanterResponse;
import com.stoktakip.dto.PersonelZimmetCreateRequest;
import com.stoktakip.dto.PersonelZimmetGecmisiResponse;
import com.stoktakip.dto.PersonelZimmetIadeRequest;
import com.stoktakip.dto.TopluIslemResponse;
import com.stoktakip.dto.TopluPersonelIadeRequest;
import com.stoktakip.dto.TopluPersonelZimmetRequest;
import com.stoktakip.entity.Envanter;
import com.stoktakip.entity.Depo;
import com.stoktakip.entity.DepoZimmetHareket;
import com.stoktakip.entity.PersonelZimmetHareket;
import com.stoktakip.enums.ZimmetHareketTipi;
import com.stoktakip.exception.BusinessRuleException;
import com.stoktakip.exception.ResourceNotFoundException;
import com.stoktakip.repository.EnvanterRepository;
import com.stoktakip.repository.DepoRepository;
import com.stoktakip.repository.DepoZimmetHareketRepository;
import com.stoktakip.repository.PersonelZimmetHareketRepository;
import com.stoktakip.service.PersonelZimmetService;
import com.stoktakip.service.TopluIslemValidator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PersonelZimmetServiceImpl implements PersonelZimmetService {

    private static final int PERSONEL_KODU_MAX_UZUNLUK = 100;

    private final PersonelZimmetHareketRepository hareketRepository;
    private final EnvanterRepository envanterRepository;
    private final DepoRepository depoRepository;
    private final DepoZimmetHareketRepository depoHareketRepository;

    public PersonelZimmetServiceImpl(
        PersonelZimmetHareketRepository hareketRepository,
        EnvanterRepository envanterRepository,
        DepoRepository depoRepository,
        DepoZimmetHareketRepository depoHareketRepository
    ) {
        this.hareketRepository = hareketRepository;
        this.envanterRepository = envanterRepository;
        this.depoRepository = depoRepository;
        this.depoHareketRepository = depoHareketRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonelEnvanterResponse> getPersonelEnvanterleri(String personelKodu) {
        return hareketRepository.findMevcutEnvanterlerByPersonelKodu(personelKodu(personelKodu));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonelZimmetGecmisiResponse> getZimmetGecmisi(String personelKodu) {
        return hareketRepository.findGecmisByPersonelKodu(personelKodu(personelKodu));
    }

    @Override
    @Transactional(readOnly = true)
    public PersonelZimmetGecmisiResponse getZimmetHareketi(String personelKodu, Long hareketId) {
        String kod = personelKodu(personelKodu);
        if (hareketId == null) {
            throw new BusinessRuleException("hareketId zorunludur");
        }
        return hareketRepository.findGecmisByPersonelKoduAndId(kod, hareketId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Personel zimmet hareketi bulunamadi: " + hareketId));
    }

    @Override
    @Transactional
    public PersonelZimmetGecmisiResponse zimmetOlustur(
        String personelKodu,
        PersonelZimmetCreateRequest request
    ) {
        String kod = personelKodu(personelKodu);
        if (request == null) {
            throw new BusinessRuleException("Zimmet talebi zorunludur");
        }
        String envanterId = envanterId(request.envanterId());
        Envanter envanter = envanteriKilitle(envanterId);
        sonHareket(envanterId).ifPresent(son -> {
            if (son.getHareketTipi() == ZimmetHareketTipi.PERSONELE_ZIMMET
                && Boolean.TRUE.equals(son.getAktif())) {
                throw new BusinessRuleException("Envanter zaten aktif olarak bir personele zimmetli");
            }
        });
        if (envanter.getDepo() == null) {
            throw new BusinessRuleException("Envanter bir depoda bulunmadığı için personele zimmetlenemez");
        }

        PersonelZimmetHareket hareket = yeniHareket(
            envanter, kod, ZimmetHareketTipi.PERSONELE_ZIMMET,
            request.aciklama(), request.islemYapanPersonel(), true);
        envanter.setDepo(null);
        return response(hareketRepository.save(hareket));
    }

    @Override
    @Transactional
    public PersonelZimmetGecmisiResponse iadeEt(
        String personelKodu,
        String envanterId,
        PersonelZimmetIadeRequest request
    ) {
        String kod = personelKodu(personelKodu);
        String id = envanterId(envanterId);
        if (request == null) {
            throw new BusinessRuleException("Iade talebi zorunludur");
        }
        Envanter envanter = envanteriKilitle(id);
        PersonelZimmetHareket son = sonHareket(id)
            .orElseThrow(() -> new BusinessRuleException("Envanterin aktif personel zimmeti bulunamadi"));
        if (son.getHareketTipi() != ZimmetHareketTipi.PERSONELE_ZIMMET
            || !Boolean.TRUE.equals(son.getAktif())) {
            throw new BusinessRuleException("Envanterin aktif personel zimmeti bulunamadi");
        }
        if (!kod.equals(son.getPersonelKodu())) {
            throw new BusinessRuleException("Envanter bu personele zimmetli degildir");
        }
        if (request.depoGuid() == null || request.depoGuid().isBlank()) {
            throw new BusinessRuleException("depoGuid zorunludur");
        }
        String depoGuid = request.depoGuid().trim();
        Depo depo = depoRepository.findById(depoGuid)
            .orElseThrow(() -> new ResourceNotFoundException("Depo bulunamadi: " + depoGuid));
        envanter.setDepo(depo);

        PersonelZimmetHareket iade = yeniHareket(
            envanter, kod, ZimmetHareketTipi.IADE,
            request.aciklama(), request.islemYapanPersonel(), false);
        PersonelZimmetHareket kaydedilen = hareketRepository.save(iade);
        DepoZimmetHareket depoHareketi = new DepoZimmetHareket();
        depoHareketi.setEnvanter(envanter);
        depoHareketi.setHareketTipi(ZimmetHareketTipi.IADE);
        depoHareketi.setZimmetTuru(envanter.getEnvanterTuru().name());
        depoHareketi.setAdet(1L);
        depoHareketi.setTur("IADE");
        depoHareketi.setGidecegiDepo(depo);
        depoHareketi.setAciklama(request.aciklama());
        depoHareketi.setIslemYapanPersonel(request.islemYapanPersonel());
        depoHareketRepository.save(depoHareketi);
        return response(kaydedilen);
    }

    @Override
    @Transactional
    public TopluIslemResponse<PersonelZimmetGecmisiResponse> topluZimmetOlustur(
        String personelKodu, TopluPersonelZimmetRequest request
    ) {
        String kod = personelKodu(personelKodu);
        if (request == null) {
            throw new BusinessRuleException("Toplu zimmet talebi zorunludur");
        }
        var items = TopluIslemValidator.validateAndSort(request.envanterler());
        List<PersonelZimmetGecmisiResponse> results = items.stream()
            .map(item -> zimmetOlustur(kod, new PersonelZimmetCreateRequest(
                item.envanterGuid(), request.aciklama(), request.islemYapanPersonel())))
            .toList();
        return new TopluIslemResponse<>("TOPLU_PERSONEL_ZIMMETI", results.size(), results);
    }

    @Override
    @Transactional
    public TopluIslemResponse<PersonelZimmetGecmisiResponse> topluIadeEt(
        String personelKodu, TopluPersonelIadeRequest request
    ) {
        String kod = personelKodu(personelKodu);
        if (request == null) {
            throw new BusinessRuleException("Toplu iade talebi zorunludur");
        }
        if (request.hedefDepoGuid() == null || request.hedefDepoGuid().isBlank()) {
            throw new BusinessRuleException("hedefDepoGuid zorunludur");
        }
        String hedefDepoGuid = request.hedefDepoGuid().trim();
        depoRepository.findById(hedefDepoGuid)
            .orElseThrow(() -> new ResourceNotFoundException("Depo bulunamadi: " + hedefDepoGuid));
        var items = TopluIslemValidator.validateAndSort(request.envanterler());
        List<PersonelZimmetGecmisiResponse> results = items.stream()
            .map(item -> iadeEt(kod, item.envanterGuid(), new PersonelZimmetIadeRequest(
                hedefDepoGuid, request.aciklama(), request.islemYapanPersonel())))
            .toList();
        return new TopluIslemResponse<>("TOPLU_PERSONEL_IADESI", results.size(), results);
    }

    private Optional<PersonelZimmetHareket> sonHareket(String envanterId) {
        return hareketRepository.findFirstByEnvanterGuidOrderByIdDesc(envanterId);
    }

    private Envanter envanteriKilitle(String envanterId) {
        return envanterRepository.findByGuidForUpdate(envanterId)
            .orElseThrow(() -> new ResourceNotFoundException("Envanter bulunamadi: " + envanterId));
    }

    private static PersonelZimmetHareket yeniHareket(
        Envanter envanter,
        String personelKodu,
        ZimmetHareketTipi hareketTipi,
        String aciklama,
        String islemYapanPersonel,
        boolean aktif
    ) {
        PersonelZimmetHareket hareket = new PersonelZimmetHareket();
        hareket.setEnvanter(envanter);
        hareket.setPersonelKodu(personelKodu);
        hareket.setHareketTipi(hareketTipi);
        hareket.setZimmetTuru(envanter.getEnvanterTuru().name());
        hareket.setAciklama(aciklama);
        hareket.setIslemYapanPersonel(islemYapanPersonel);
        hareket.setAktif(aktif);
        return hareket;
    }

    private static PersonelZimmetGecmisiResponse response(PersonelZimmetHareket hareket) {
        Envanter envanter = hareket.getEnvanter();
        var model = envanter.getModel();
        String modelAdi = model == null ? null : model.getModelAdi();
        String markaAdi = model == null || model.getMarka() == null
            ? null
            : model.getMarka().getMarkaAdi();
        return new PersonelZimmetGecmisiResponse(
            hareket.getId(), hareket.getPersonelKodu(), envanter.getGuid(),
            envanter.getEnvanterAdi(), envanter.getEnvanterTuru(), markaAdi, modelAdi,
            null, null,
            hareket.getHareketTipi(), hareket.getZimmetTuru(), hareket.getAciklama(),
            hareket.getIslemYapanPersonel(), hareket.getOlusturmaTarihi(),
            Boolean.TRUE.equals(hareket.getAktif()));
    }

    private static String personelKodu(String personelKodu) {
        if (personelKodu == null || personelKodu.isBlank()) {
            throw new BusinessRuleException("personelKodu zorunludur");
        }
        String kod = personelKodu.trim();
        if (kod.length() > PERSONEL_KODU_MAX_UZUNLUK) {
            throw new BusinessRuleException("personelKodu en fazla 100 karakter olabilir");
        }
        return kod;
    }

    private static String envanterId(String envanterId) {
        if (envanterId == null || envanterId.isBlank()) {
            throw new BusinessRuleException("envanterId zorunludur");
        }
        return envanterId.trim();
    }
}
