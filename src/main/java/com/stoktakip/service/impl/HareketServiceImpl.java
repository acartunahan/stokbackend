package com.stoktakip.service.impl;

import com.stoktakip.dto.DepoHareketResponse;
import com.stoktakip.dto.EnvanterHareketResponse;
import com.stoktakip.dto.PageResponse;
import com.stoktakip.entity.Depo;
import com.stoktakip.entity.DepoZimmetHareket;
import com.stoktakip.entity.Envanter;
import com.stoktakip.entity.PersonelZimmetHareket;
import com.stoktakip.exception.BusinessRuleException;
import com.stoktakip.exception.ResourceNotFoundException;
import com.stoktakip.repository.DepoRepository;
import com.stoktakip.repository.DepoZimmetHareketRepository;
import com.stoktakip.repository.EnvanterRepository;
import com.stoktakip.repository.PersonelZimmetHareketRepository;
import com.stoktakip.service.HareketService;
import com.stoktakip.service.PageableFactory;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class HareketServiceImpl implements HareketService {
    private static final Set<String> HAREKET_SORT_FIELDS = Set.of(
        "olusturmaTarihi", "id", "hareketTipi", "adet"
    );
    private static final Sort TIMELINE_SORT = Sort.by(
        Sort.Order.desc("olusturmaTarihi"), Sort.Order.desc("id"));

    private final EnvanterRepository envanterRepository;
    private final DepoRepository depoRepository;
    private final PersonelZimmetHareketRepository personelHareketRepository;
    private final DepoZimmetHareketRepository depoHareketRepository;

    public HareketServiceImpl(
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

    @Override
    public PageResponse<EnvanterHareketResponse> getEnvanterHareketleri(
        String envanterGuid,
        LocalDateTime baslangicTarihi,
        LocalDateTime bitisTarihi,
        Pageable pageable
    ) {
        String guid = required(envanterGuid, "envanterGuid");
        validateDates(baslangicTarihi, bitisTarihi);
        envanterRepository.findById(guid)
            .orElseThrow(() -> new ResourceNotFoundException("Envanter bulunamadı: " + guid));
        Pageable safe = PageableFactory.sanitize(pageable, TIMELINE_SORT,
            Set.of("olusturmaTarihi", "id"));
        long fetchSizeLong = (long) (safe.getPageNumber() + 1) * safe.getPageSize();
        if (fetchSizeLong > Integer.MAX_VALUE) {
            throw new BusinessRuleException("İstenen sayfa numarası çok büyük");
        }
        Pageable sourcePage = PageRequest.of(0, (int) fetchSizeLong, safe.getSort());
        Page<PersonelZimmetHareket> personelPage = personelHareketRepository
            .findTimelineByEnvanterGuid(guid, baslangicTarihi, bitisTarihi, sourcePage);
        Page<DepoZimmetHareket> depoPage = depoHareketRepository
            .findTimelineByEnvanterGuid(guid, baslangicTarihi, bitisTarihi, sourcePage);

        List<EnvanterHareketResponse> merged = new ArrayList<>(
            personelPage.getNumberOfElements() + depoPage.getNumberOfElements());
        personelPage.forEach(h -> merged.add(map(h)));
        depoPage.forEach(h -> merged.add(map(h)));
        merged.sort(timelineComparator(safe.getSort()));

        int from = Math.min(safe.getPageNumber() * safe.getPageSize(), merged.size());
        int to = Math.min(from + safe.getPageSize(), merged.size());
        long total = personelPage.getTotalElements() + depoPage.getTotalElements();
        return PageResponse.of(merged.subList(from, to), safe.getPageNumber(),
            safe.getPageSize(), total);
    }

    @Override
    public PageResponse<DepoHareketResponse> getDepoHareketleri(
        String depoGuid,
        LocalDateTime baslangicTarihi,
        LocalDateTime bitisTarihi,
        Pageable pageable
    ) {
        String guid = required(depoGuid, "depoGuid");
        validateDates(baslangicTarihi, bitisTarihi);
        depoRepository.findById(guid)
            .orElseThrow(() -> new ResourceNotFoundException("Depo bulunamadı: " + guid));
        Pageable safe = PageableFactory.sanitize(pageable, TIMELINE_SORT, HAREKET_SORT_FIELDS);
        Page<DepoHareketResponse> result = depoHareketRepository
            .findByDepoGuid(guid, baslangicTarihi, bitisTarihi, safe)
            .map(h -> map(h, guid));
        return PageResponse.from(result);
    }

    private static EnvanterHareketResponse map(PersonelZimmetHareket hareket) {
        Envanter envanter = hareket.getEnvanter();
        return new EnvanterHareketResponse(
            hareket.getId(), "PERSONEL", envanter.getGuid(), hareket.getHareketTipi(),
            hareket.getOlusturmaTarihi(), hareket.getPersonelKodu(), null, null,
            null, null, null, hareket.getAciklama(), hareket.getIslemYapanPersonel(),
            Boolean.TRUE.equals(hareket.getAktif()));
    }

    private static EnvanterHareketResponse map(DepoZimmetHareket hareket) {
        return new EnvanterHareketResponse(
            hareket.getId(), "DEPO", hareket.getEnvanter().getGuid(),
            hareket.getHareketTipi(), hareket.getOlusturmaTarihi(), null,
            guid(hareket.getMevcutDepo()), ad(hareket.getMevcutDepo()),
            guid(hareket.getGidecegiDepo()), ad(hareket.getGidecegiDepo()),
            hareket.getAdet(), hareket.getAciklama(), hareket.getIslemYapanPersonel(),
            Boolean.TRUE.equals(hareket.getAktif()));
    }

    private static DepoHareketResponse map(DepoZimmetHareket hareket, String depoGuid) {
        Depo kaynak = hareket.getMevcutDepo();
        Depo hedef = hareket.getGidecegiDepo();
        return new DepoHareketResponse(
            hareket.getId(), hareket.getEnvanter().getGuid(),
            hareket.getEnvanter().getEnvanterAdi(), hareket.getHareketTipi(),
            direction(kaynak, hedef, depoGuid), hareket.getOlusturmaTarihi(),
            guid(kaynak), ad(kaynak), guid(hedef), ad(hedef), hareket.getAdet(),
            hareket.getAciklama(), hareket.getIslemYapanPersonel());
    }

    private static String direction(Depo kaynak, Depo hedef, String depoGuid) {
        boolean source = kaynak != null && depoGuid.equals(kaynak.getGuid());
        boolean target = hedef != null && depoGuid.equals(hedef.getGuid());
        if (source && target) return "GIRIS_CIKIS";
        if (target) return "GIRIS";
        if (source) return "CIKIS";
        throw new IllegalStateException("Hareket istenen depoyla ilişkili değil");
    }

    private static void validateDates(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new BusinessRuleException("bitisTarihi baslangicTarihi değerinden önce olamaz");
        }
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new BusinessRuleException(field + " zorunludur");
        }
        return value.trim();
    }

    private static String guid(Depo depo) {
        return depo == null ? null : depo.getGuid();
    }

    private static String ad(Depo depo) {
        return depo == null ? null : depo.getAd();
    }

    private static Comparator<EnvanterHareketResponse> timelineComparator(Sort sort) {
        Comparator<EnvanterHareketResponse> comparator = null;
        for (Sort.Order order : sort) {
            Comparator<EnvanterHareketResponse> next = switch (order.getProperty()) {
                case "olusturmaTarihi" -> Comparator.comparing(
                    EnvanterHareketResponse::tarih,
                    Comparator.nullsLast(Comparator.naturalOrder()));
                case "id" -> Comparator.comparing(
                    EnvanterHareketResponse::hareketId,
                    Comparator.nullsLast(Comparator.naturalOrder()));
                default -> throw new BusinessRuleException(
                    "Desteklenmeyen sıralama alanı: " + order.getProperty());
            };
            if (order.isDescending()) {
                next = next.reversed();
            }
            comparator = comparator == null ? next : comparator.thenComparing(next);
        }
        Comparator<EnvanterHareketResponse> stableTieBreaker = Comparator.comparing(
            EnvanterHareketResponse::kaynakTuru, Comparator.nullsLast(String::compareTo));
        return comparator == null ? stableTieBreaker : comparator.thenComparing(stableTieBreaker);
    }
}
