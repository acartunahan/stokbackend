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
        aktif.setAktif(false);
        personelHareketRepository.save(aktif);

        if (talep.depoGuid() != null && !talep.depoGuid().isBlank()) {
            envanter.setDepo(depo(talep.depoGuid()));
        }
        PersonelZimmetHareket iade = yeniPersonelHareket(envanter, ZimmetHareketTipi.IADE);
        iade.setPersonelKodu(aktif.getPersonelKodu());
        iade.setAciklama(talep.aciklama());
        iade.setIslemYapanPersonel(talep.islemYapanPersonel());
        iade.setAktif(false);
        return yanit(personelHareketRepository.save(iade), envanter);
    }

    @Transactional
    public ZimmetHareketYaniti depoyaTasi(DepoTransferTalebi talep) {
        String envanterGuid = zorunluEnvanterGuid(talep, talep == null ? null : talep.envanterGuid());
        if (talep.gidecegiDepoGuid() == null || talep.gidecegiDepoGuid().isBlank()) {
            throw new BusinessRuleException("gidecegiDepoGuid zorunludur");
        }
        Envanter envanter = envanteriKilitle(envanterGuid);
        Depo mevcutDepo = envanter.getDepo();
        aktifZimmet(envanterGuid).ifPresent(aktif -> {
            aktif.setAktif(false);
            personelHareketRepository.save(aktif);
        });
        Depo gidecegiDepo = depo(talep.gidecegiDepoGuid());
        envanter.setDepo(gidecegiDepo);

        DepoZimmetHareket hareket = new DepoZimmetHareket();
        hareket.setEnvanter(envanter);
        hareket.setHareketTipi(ZimmetHareketTipi.DEPO_TRANSFERI);
        hareket.setZimmetTuru(envanter.getEnvanterTuru().name());
        hareket.setMevcutDepo(mevcutDepo);
        hareket.setGidecegiDepo(gidecegiDepo);
        hareket.setAciklama(talep.aciklama());
        hareket.setIslemYapanPersonel(talep.islemYapanPersonel());
        return yanit(depoHareketRepository.save(hareket), envanter);
    }

    @Transactional(readOnly = true)
    public List<ZimmetHareketYaniti> personelGecmisi(String guid) {
        Envanter envanter = envanteriBul(guid);
        return personelHareketRepository.findByEnvanterGuidOrderByIdDesc(guid).stream()
            .map(hareket -> yanit(hareket, envanter))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ZimmetHareketYaniti> depoGecmisi(String guid) {
        Envanter envanter = envanteriBul(guid);
        return depoHareketRepository.findByEnvanterGuidOrderByIdDesc(guid).stream()
            .map(hareket -> yanit(hareket, envanter))
            .toList();
    }

    private Optional<PersonelZimmetHareket> aktifZimmet(String envanterGuid) {
        return personelHareketRepository.findFirstByEnvanterGuidAndAktifTrueOrderByIdDesc(envanterGuid);
    }

    private PersonelZimmetHareket yeniPersonelHareket(Envanter envanter, ZimmetHareketTipi tip) {
        PersonelZimmetHareket hareket = new PersonelZimmetHareket();
        hareket.setEnvanter(envanter);
        hareket.setHareketTipi(tip);
        hareket.setZimmetTuru(envanter.getEnvanterTuru().name());
        return hareket;
    }

    private Envanter envanteriKilitle(String guid) {
        return envanterRepository.findByGuidForUpdate(guid)
            .orElseThrow(() -> new BusinessRuleException("Envanter bulunamadi: " + guid));
    }

    private Envanter envanteriBul(String guid) {
        zorunluEnvanterGuid(guid, guid);
        return envanterRepository.findById(guid)
            .orElseThrow(() -> new BusinessRuleException("Envanter bulunamadi: " + guid));
    }

    private Depo depo(String guid) {
        return depoRepository.findById(guid)
            .orElseThrow(() -> new BusinessRuleException("Depo bulunamadi: " + guid));
    }

    private static String zorunluEnvanterGuid(Object talep, String guid) {
        if (talep == null || guid == null || guid.isBlank()) {
            throw new BusinessRuleException("envanterGuid zorunludur");
        }
        return guid.trim();
    }

    private static ZimmetHareketYaniti yanit(PersonelZimmetHareket hareket, Envanter envanter) {
        return new ZimmetHareketYaniti(hareket.getId(), envanter.getGuid(), envanter.getEnvanterTuru().name(),
            hareket.getHareketTipi(), hareket.getPersonelKodu(), null, null, hareket.getAciklama(),
            hareket.getOlusturmaTarihi(), Boolean.TRUE.equals(hareket.getAktif()));
    }

    private static ZimmetHareketYaniti yanit(DepoZimmetHareket hareket, Envanter envanter) {
        return new ZimmetHareketYaniti(hareket.getId(), envanter.getGuid(), envanter.getEnvanterTuru().name(),
            hareket.getHareketTipi(), null, guid(hareket.getMevcutDepo()), guid(hareket.getGidecegiDepo()),
            hareket.getAciklama(), hareket.getOlusturmaTarihi(), Boolean.TRUE.equals(hareket.getAktif()));
    }

    private static String guid(Depo depo) {
        return depo == null ? null : depo.getGuid();
    }
}
