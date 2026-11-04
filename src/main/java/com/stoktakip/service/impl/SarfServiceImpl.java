package com.stoktakip.service.impl;

import com.stoktakip.dto.SarfHareketRequest;
import com.stoktakip.dto.SarfHareketResponse;
import com.stoktakip.dto.SarfStokResponse;
import com.stoktakip.entity.Depo;
import com.stoktakip.entity.DepoZimmetHareket;
import com.stoktakip.entity.Envanter;
import com.stoktakip.entity.SarfLogKaydi;
import com.stoktakip.enums.EnvanterTuru;
import com.stoktakip.enums.ZimmetHareketTipi;
import com.stoktakip.exception.BusinessRuleException;
import com.stoktakip.exception.ResourceNotFoundException;
import com.stoktakip.repository.DepoRepository;
import com.stoktakip.repository.DepoZimmetHareketRepository;
import com.stoktakip.repository.EnvanterRepository;
import com.stoktakip.repository.SarfLogKaydiRepository;
import com.stoktakip.service.SarfService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Sarf malzeme stoku DEPO_ZIMMET_HAREKET üzerindeki TUR/ADET kayıtlarından hesaplanır.
 * Eski R_SARFDEMIRBAS hareketleri de aynı tabloya göç ettiği için eski ve yeni hareketler birlikte sayılır.
 */
@Service
@Transactional(readOnly = true)
public class SarfServiceImpl implements SarfService {
    static final Set<String> GIRIS_TURLERI = Set.of("GIRIS", "GELEN", "G", "1");
    static final Set<String> CIKIS_TURLERI = Set.of("CIKIS", "GIDEN", "C", "-1");

    private final DepoZimmetHareketRepository hareketRepository;
    private final EnvanterRepository envanterRepository;
    private final DepoRepository depoRepository;
    private final SarfLogKaydiRepository logRepository;

    public SarfServiceImpl(
        DepoZimmetHareketRepository hareketRepository, EnvanterRepository envanterRepository,
        DepoRepository depoRepository, SarfLogKaydiRepository logRepository
    ) {
        this.hareketRepository = hareketRepository;
        this.envanterRepository = envanterRepository;
        this.depoRepository = depoRepository;
        this.logRepository = logRepository;
    }

    @Override
    @Transactional
    public SarfHareketResponse giris(SarfHareketRequest request) {
        if (request == null) throw new BusinessRuleException("Sarf giriş talebi zorunludur");
        Envanter envanter = sarfEnvanter(request.envanterGuid());
        Depo depo = depo(request.depoGuid());
        DepoZimmetHareket hareket = yeniHareket(envanter, request, "GIRIS");
        hareket.setGidecegiDepo(depo);
        DepoZimmetHareket kaydedilen = hareketRepository.save(hareket);
        log(envanter, depo, request, "GIRIS");
        return toResponse(kaydedilen, depo, stok(envanter.getGuid(), depo.getGuid()));
    }

    @Override
    @Transactional
    public SarfHareketResponse cikis(SarfHareketRequest request) {
        if (request == null) throw new BusinessRuleException("Sarf çıkış talebi zorunludur");
        Envanter envanter = sarfEnvanter(request.envanterGuid());
        Depo depo = depo(request.depoGuid());
        long mevcut = stok(envanter.getGuid(), depo.getGuid());
        if (mevcut < request.adet()) {
            throw new BusinessRuleException("Yetersiz sarf stoku. Depodaki miktar: " + mevcut
                + ", istenen: " + request.adet());
        }
        DepoZimmetHareket hareket = yeniHareket(envanter, request, "CIKIS");
        hareket.setMevcutDepo(depo);
        DepoZimmetHareket kaydedilen = hareketRepository.save(hareket);
        log(envanter, depo, request, "CIKIS");
        return toResponse(kaydedilen, depo, mevcut - request.adet());
    }

    @Override
    public List<SarfStokResponse> getStok(String depoGuid, String envanterGuid) {
        String depoId = blankToNull(depoGuid);
        String envanterId = blankToNull(envanterGuid);
        Map<List<String>, long[]> toplamlar = new HashMap<>();
        for (Object[] row : hareketRepository.sumSarfGiris(GIRIS_TURLERI, depoId, envanterId)) {
            toplamlar.computeIfAbsent(anahtar(row), key -> new long[2])[0] += sayi(row[2]);
        }
        for (Object[] row : hareketRepository.sumSarfCikis(CIKIS_TURLERI, depoId, envanterId)) {
            toplamlar.computeIfAbsent(anahtar(row), key -> new long[2])[1] += sayi(row[2]);
        }

        Set<String> envanterGuidleri = new HashSet<>();
        Set<String> depoGuidleri = new HashSet<>();
        toplamlar.keySet().forEach(key -> {
            envanterGuidleri.add(key.get(0));
            depoGuidleri.add(key.get(1));
        });
        Map<String, Envanter> envanterler = new HashMap<>();
        envanterRepository.findAllById(envanterGuidleri).forEach(item -> envanterler.put(item.getGuid(), item));
        Map<String, Depo> depolar = new HashMap<>();
        depoRepository.findAllById(depoGuidleri).forEach(item -> depolar.put(item.getGuid(), item));

        List<SarfStokResponse> sonuc = new ArrayList<>();
        toplamlar.forEach((key, toplam) -> {
            Envanter envanter = envanterler.get(key.get(0));
            Depo depo = depolar.get(key.get(1));
            sonuc.add(new SarfStokResponse(key.get(0), envanter == null ? null : envanter.getEnvanterAdi(),
                key.get(1), depo == null ? null : depo.getAd(), toplam[0], toplam[1], toplam[0] - toplam[1]));
        });
        sonuc.sort(Comparator.comparing(SarfStokResponse::depoGuid)
            .thenComparing(item -> item.envanterAdi() == null ? "" : item.envanterAdi()));
        return sonuc;
    }

    private long stok(String envanterGuid, String depoGuid) {
        return getStok(depoGuid, envanterGuid).stream().mapToLong(SarfStokResponse::stok).sum();
    }

    private DepoZimmetHareket yeniHareket(Envanter envanter, SarfHareketRequest request, String tur) {
        DepoZimmetHareket hareket = new DepoZimmetHareket();
        hareket.setEnvanter(envanter);
        hareket.setHareketTipi(ZimmetHareketTipi.DEPO_TRANSFERI);
        hareket.setZimmetTuru(EnvanterTuru.SARF.name());
        hareket.setAdet(request.adet());
        hareket.setTur(tur);
        hareket.setAciklama(request.aciklama());
        hareket.setIslemYapanPersonel(request.personelKodu());
        return hareket;
    }

    private void log(Envanter envanter, Depo depo, SarfHareketRequest request, String islem) {
        SarfLogKaydi kayit = new SarfLogKaydi();
        kayit.setGuid(UUID.randomUUID().toString());
        kayit.setOlusturmaTarihi(LocalDateTime.now());
        kayit.setDurum("AKTIF");
        kayit.setPersonelKodu(request.personelKodu());
        kayit.setIslem(islem);
        kayit.setAciklama(request.adet() + " adet " + envanter.getEnvanterAdi()
            + (request.aciklama() == null ? "" : " - " + request.aciklama()));
        kayit.setSarfMalzemeGuid(envanter.getGuid());
        kayit.setDepo(depo);
        logRepository.save(kayit);
    }

    private Envanter sarfEnvanter(String guid) {
        String id = id(guid, "envanterGuid");
        Envanter envanter = envanterRepository.findByGuidForUpdate(id)
            .orElseThrow(() -> new ResourceNotFoundException("Envanter bulunamadı: " + id));
        if (envanter.getEnvanterTuru() != EnvanterTuru.SARF) {
            throw new BusinessRuleException("Sadece SARF türündeki envanterler için stok hareketi yapılabilir");
        }
        return envanter;
    }

    private Depo depo(String guid) {
        String id = id(guid, "depoGuid");
        return depoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Depo bulunamadı: " + id));
    }

    private static SarfHareketResponse toResponse(DepoZimmetHareket hareket, Depo depo, long kalanStok) {
        return new SarfHareketResponse(hareket.getId(), hareket.getEnvanter().getGuid(),
            hareket.getEnvanter().getEnvanterAdi(), depo.getGuid(), hareket.getTur(), hareket.getAdet(),
            kalanStok, hareket.getOlusturmaTarihi());
    }

    private static List<String> anahtar(Object[] row) {
        return List.of(String.valueOf(row[0]), String.valueOf(row[1]));
    }

    private static long sayi(Object value) {
        return value instanceof Number number ? number.longValue() : 0L;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String id(String value, String field) {
        if (value == null || value.isBlank()) throw new BusinessRuleException(field + " zorunludur");
        return value.trim();
    }
}
