package com.stoktakip.service;

import com.stoktakip.dto.DepoTransferTalebi;
import com.stoktakip.dto.IadeTalebi;
import com.stoktakip.dto.ZimmetHareketYaniti;
import com.stoktakip.dto.ZimmetTalebi;
import com.stoktakip.entity.Depo;
import com.stoktakip.entity.DepoZimmetHareket;
import com.stoktakip.entity.Envanter;
import com.stoktakip.entity.PersonelZimmetHareket;
import com.stoktakip.enums.ZimmetHareketTipi;
import com.stoktakip.exception.BusinessRuleException;
import com.stoktakip.exception.ResourceNotFoundException;
import com.stoktakip.repository.DepoRepository;
import com.stoktakip.repository.DepoZimmetHareketRepository;
import com.stoktakip.repository.EnvanterRepository;
import com.stoktakip.repository.PersonelZimmetHareketRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ZimmetService {

    private final EnvanterRepository envanterRepository;
    private final DepoRepository depoRepository;
    private final PersonelZimmetHareketRepository personelHareketRepository;
    private final DepoZimmetHareketRepository depoHareketRepository;

    public ZimmetService(
        EnvanterRepository envanterRepository,
        DepoRepository depoRepository,
        PersonelZimmetHareketRepository personelHareketRepository,
        DepoZimmetHareketRepository depoHareketRepository
    ) {
        this.envanterRepository = envanterRepository;
        this.depoRepository = depoRepository;
        this.personelHareketRepository = personelHareketRepository;
        this.depoHareketRepository = depoHareketRepository;
    }

    @Transactional
    public ZimmetHareketYaniti personeleZimmetle(ZimmetTalebi talep) {
        String envanterGuid = zorunluEnvanterGuid(talep, talep == null ? null : talep.envanterGuid());
        if (talep.personelKodu() == null || talep.personelKodu().isBlank()) {
            throw new BusinessRuleException("personelKodu zorunludur");
        }
        Envanter envanter = envanteriKilitle(envanterGuid);
        if (aktifZimmet(envanterGuid).isPresent()) {
            throw new BusinessRuleException("Envanter zaten aktif olarak bir personele zimmetli");
        }
        if (envanter.getDepo() == null) {
            throw new BusinessRuleException("Envanter bir depoda bulunmadığı için personele zimmetlenemez");
        }

        envanter.setDepo(null);
        PersonelZimmetHareket hareket = yeniPersonelHareket(envanter, ZimmetHareketTipi.PERSONELE_ZIMMET);
        hareket.setPersonelKodu(talep.personelKodu().trim());
        hareket.setAciklama(talep.aciklama());
        hareket.setIslemYapanPersonel(talep.islemYapanPersonel());
        hareket.setAktif(true);
        return yanit(personelHareketRepository.save(hareket), envanter);
    }

    @Transactional
    public ZimmetHareketYaniti iadeAl(IadeTalebi talep) {
        String envanterGuid = zorunluEnvanterGuid(talep, talep == null ? null : talep.envanterGuid());
        Envanter envanter = envanteriKilitle(envanterGuid);
        PersonelZimmetHareket aktif = aktifZimmet(envanterGuid)
            .orElseThrow(() -> new BusinessRuleException("Envanterin aktif personel zimmeti bulunamadi"));

        if (talep.depoGuid() == null || talep.depoGuid().isBlank()) {
            throw new BusinessRuleException("depoGuid zorunludur");
        }
        Depo hedefDepo = depo(talep.depoGuid());
        envanter.setDepo(hedefDepo);
        PersonelZimmetHareket iade = yeniPersonelHareket(envanter, ZimmetHareketTipi.IADE);
        iade.setPersonelKodu(aktif.getPersonelKodu());
        iade.setAciklama(talep.aciklama());
        iade.setIslemYapanPersonel(talep.islemYapanPersonel());
        iade.setAktif(false);
        PersonelZimmetHareket kaydedilenIade = personelHareketRepository.save(iade);

        DepoZimmetHareket depoHareketi = yeniDepoHareket(
            envanter, ZimmetHareketTipi.IADE, null, hedefDepo,
            talep.aciklama(), talep.islemYapanPersonel(), "IADE");
        depoHareketRepository.save(depoHareketi);
        return yanit(kaydedilenIade, envanter);
    }

    @Transactional
    public ZimmetHareketYaniti depoyaTasi(DepoTransferTalebi talep) {
        String envanterGuid = zorunluEnvanterGuid(talep, talep == null ? null : talep.envanterGuid());
        if (talep.gidecegiDepoGuid() == null || talep.gidecegiDepoGuid().isBlank()) {
            throw new BusinessRuleException("gidecegiDepoGuid zorunludur");
        }
        Envanter envanter = envanteriKilitle(envanterGuid);
        Depo mevcutDepo = envanter.getDepo();
        if (aktifZimmet(envanterGuid).isPresent() || mevcutDepo == null) {
            throw new BusinessRuleException("Yalnızca depoda bulunan envanter transfer edilebilir");
        }
        Depo gidecegiDepo = depo(talep.gidecegiDepoGuid());
        if (mevcutDepo.getGuid().equals(gidecegiDepo.getGuid())) {
            throw new BusinessRuleException("Kaynak ve hedef depo aynı olamaz");
        }
        envanter.setDepo(gidecegiDepo);

        DepoZimmetHareket hareket = yeniDepoHareket(
            envanter, ZimmetHareketTipi.DEPO_TRANSFERI, mevcutDepo, gidecegiDepo,
            talep.aciklama(), talep.islemYapanPersonel(), "TRANSFER");
        return yanit(depoHareketRepository.save(hareket), envanter);
    }

    @Transactional(readOnly = true)
    public List<ZimmetHareketYaniti> personelGecmisi(String guid) {
        Envanter envanter = envanteriBul(guid);
        return personelHareketRepository.findByEnvanterGuidOrderByIdDesc(envanter.getGuid()).stream()
            .map(hareket -> yanit(hareket, envanter))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ZimmetHareketYaniti> depoGecmisi(String guid) {
        Envanter envanter = envanteriBul(guid);
        return depoHareketRepository.findByEnvanterGuidOrderByIdDesc(envanter.getGuid()).stream()
            .map(hareket -> yanit(hareket, envanter))
            .toList();
    }

    private Optional<PersonelZimmetHareket> aktifZimmet(String envanterGuid) {
        return personelHareketRepository.findFirstByEnvanterGuidOrderByIdDesc(envanterGuid)
            .filter(hareket -> hareket.getHareketTipi() == ZimmetHareketTipi.PERSONELE_ZIMMET)
            .filter(hareket -> Boolean.TRUE.equals(hareket.getAktif()));
    }

    private PersonelZimmetHareket yeniPersonelHareket(Envanter envanter, ZimmetHareketTipi tip) {
        PersonelZimmetHareket hareket = new PersonelZimmetHareket();
        hareket.setEnvanter(envanter);
        hareket.setHareketTipi(tip);
        hareket.setZimmetTuru(envanter.getEnvanterTuru().name());
        return hareket;
    }

    private static DepoZimmetHareket yeniDepoHareket(
        Envanter envanter, ZimmetHareketTipi tip, Depo mevcutDepo, Depo hedefDepo,
        String aciklama, String islemYapanPersonel, String tur
    ) {
        DepoZimmetHareket hareket = new DepoZimmetHareket();
        hareket.setEnvanter(envanter);
        hareket.setHareketTipi(tip);
        hareket.setZimmetTuru(envanter.getEnvanterTuru().name());
        hareket.setAdet(1L);
        hareket.setTur(tur);
        hareket.setMevcutDepo(mevcutDepo);
        hareket.setGidecegiDepo(hedefDepo);
        hareket.setAciklama(aciklama);
        hareket.setIslemYapanPersonel(islemYapanPersonel);
        return hareket;
    }

    private Envanter envanteriKilitle(String guid) {
        return envanterRepository.findByGuidForUpdate(guid)
            .orElseThrow(() -> new ResourceNotFoundException("Envanter bulunamadi: " + guid));
    }

    private Envanter envanteriBul(String guid) {
        String normalizedGuid = zorunluEnvanterGuid(guid, guid);
        return envanterRepository.findById(normalizedGuid)
            .orElseThrow(() -> new ResourceNotFoundException("Envanter bulunamadi: " + normalizedGuid));
    }

    private Depo depo(String guid) {
        String normalizedGuid = guid.trim();
        return depoRepository.findById(normalizedGuid)
            .orElseThrow(() -> new ResourceNotFoundException("Depo bulunamadi: " + normalizedGuid));
    }

    private static String zorunluEnvanterGuid(Object talep, String guid) {
        if (talep == null || guid == null || guid.isBlank()) {
            throw new BusinessRuleException("envanterGuid zorunludur");
        }
        return guid.trim();
    }

    private static ZimmetHareketYaniti yanit(PersonelZimmetHareket hareket, Envanter envanter) {
        return new ZimmetHareketYaniti(hareket.getId(), envanter.getGuid(), envanter.getEnvanterTuru().name(),
            hareket.getHareketTipi(), hareket.getPersonelKodu(), null, null, null, hareket.getAciklama(),
            hareket.getOlusturmaTarihi(), Boolean.TRUE.equals(hareket.getAktif()));
    }

    private static ZimmetHareketYaniti yanit(DepoZimmetHareket hareket, Envanter envanter) {
        return new ZimmetHareketYaniti(hareket.getId(), envanter.getGuid(), envanter.getEnvanterTuru().name(),
            hareket.getHareketTipi(), null, guid(hareket.getMevcutDepo()), guid(hareket.getGidecegiDepo()),
            hareket.getAdet(), hareket.getAciklama(), hareket.getOlusturmaTarihi(), Boolean.TRUE.equals(hareket.getAktif()));
    }

    private static String guid(Depo depo) {
        return depo == null ? null : depo.getGuid();
    }
}
