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
import com.stoktakip.service.Guidler;
import com.stoktakip.service.SarfService;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
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
        String depoId = Guidler.bosIseNull(depoGuid);
        String envanterId = Guidler.bosIseNull(envanterGuid);
        Map<StokAnahtari, long[]> toplamlar = new LinkedHashMap<>();
        for (Object[] row : hareketRepository.sumSarfGiris(GIRIS_TURLERI, depoId, envanterId)) {
            toplamlar.computeIfAbsent(StokAnahtari.from(row), key -> new long[2])[0] += sayi(row[4]);
        }
        for (Object[] row : hareketRepository.sumSarfCikis(CIKIS_TURLERI, depoId, envanterId)) {
            toplamlar.computeIfAbsent(StokAnahtari.from(row), key -> new long[2])[1] += sayi(row[4]);
        }
        return toplamlar.entrySet().stream()
            .map(entry -> entry.getKey().toResponse(entry.getValue()[0], entry.getValue()[1]))
            .sorted(Comparator.comparing(SarfStokResponse::depoGuid)
                .thenComparing(item -> item.envanterAdi() == null ? "" : item.envanterAdi()))
            .toList();
    }

    private long stok(String envanterGuid, String depoGuid) {
        return getStok(depoGuid, envanterGuid).stream().mapToLong(SarfStokResponse::stok).sum();
    }

    private DepoZimmetHareket yeniHareket(Envanter envanter, SarfHareketRequest request, String tur) {
        DepoZimmetHareket hareket = DepoZimmetHareket.yeni(
            envanter, ZimmetHareketTipi.DEPO_TRANSFERI, tur, request.adet(), null, null);
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
        String id = Guidler.zorunlu(guid, "envanterGuid");
        Envanter envanter = envanterRepository.findByGuidForUpdate(id)
            .orElseThrow(() -> new ResourceNotFoundException("Envanter bulunamadı: " + id));
        if (envanter.getEnvanterTuru() != EnvanterTuru.SARF) {
            throw new BusinessRuleException("Sadece SARF türündeki envanterler için stok hareketi yapılabilir");
        }
        return envanter;
    }

    private Depo depo(String guid) {
        String id = Guidler.zorunlu(guid, "depoGuid");
        return depoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Depo bulunamadı: " + id));
    }

    private static SarfHareketResponse toResponse(DepoZimmetHareket hareket, Depo depo, long kalanStok) {
        return new SarfHareketResponse(hareket.getId(), hareket.getEnvanter().getGuid(),
            hareket.getEnvanter().getEnvanterAdi(), depo.getGuid(), hareket.getTur(), hareket.getAdet(),
            kalanStok, hareket.getOlusturmaTarihi());
    }

    /** Toplama sorgularının satırları: envanterGuid, envanterAdi, depoGuid, depoAdi, adet. */
    private record StokAnahtari(String envanterGuid, String envanterAdi, String depoGuid, String depoAdi) {
        static StokAnahtari from(Object[] row) {
            return new StokAnahtari((String) row[0], (String) row[1], (String) row[2], (String) row[3]);
        }

        SarfStokResponse toResponse(long giris, long cikis) {
            return new SarfStokResponse(envanterGuid, envanterAdi, depoGuid, depoAdi, giris, cikis, giris - cikis);
        }
    }

    private static long sayi(Object value) {
        return value instanceof Number number ? number.longValue() : 0L;
    }
}
