package com.stoktakip.service.impl;

import com.stoktakip.dto.ArizaCreateRequest;
import com.stoktakip.dto.ArizaDetailResponse;
import com.stoktakip.dto.ArizaDurumuCreateRequest;
import com.stoktakip.dto.ArizaDurumuResponse;
import com.stoktakip.dto.ArizaEnvanterResponse;
import com.stoktakip.dto.ArizaFilter;
import com.stoktakip.dto.ArizaKapatRequest;
import com.stoktakip.dto.ArizaResponse;
import com.stoktakip.dto.ArizaSahiplikResponse;
import com.stoktakip.dto.PageResponse;
import com.stoktakip.entity.Ariza;
import com.stoktakip.entity.ArizaDurumu;
import com.stoktakip.entity.Envanter;
import com.stoktakip.enums.ArizaDurumTipi;
import com.stoktakip.exception.BusinessRuleException;
import com.stoktakip.exception.ResourceNotFoundException;
import com.stoktakip.repository.ArizaDurumuRepository;
import com.stoktakip.repository.ArizaRepository;
import com.stoktakip.repository.ArizaSpecifications;
import com.stoktakip.repository.EnvanterRepository;
import com.stoktakip.service.ArizaService;
import com.stoktakip.service.ArizaTransitionValidator;
import com.stoktakip.service.HistoricalOwnershipService;
import com.stoktakip.service.PageableFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ArizaServiceImpl implements ArizaService {
    private static final Set<String> SORT_FIELDS = Set.of(
        "arizaTarihi", "sonDurum", "kapandiMi", "kapanisTarihi", "guid");
    private static final Sort DEFAULT_SORT = Sort.by(
        Sort.Order.desc("arizaTarihi"), Sort.Order.desc("guid"));

    private final ArizaRepository arizaRepository;
    private final ArizaDurumuRepository durumRepository;
    private final EnvanterRepository envanterRepository;
    private final HistoricalOwnershipService ownershipService;

    public ArizaServiceImpl(ArizaRepository arizaRepository,
                            ArizaDurumuRepository durumRepository,
                            EnvanterRepository envanterRepository,
                            HistoricalOwnershipService ownershipService) {
        this.arizaRepository = arizaRepository;
        this.durumRepository = durumRepository;
        this.envanterRepository = envanterRepository;
        this.ownershipService = ownershipService;
    }

    @Override
    @Transactional
    public ArizaResponse create(String envanterGuid, ArizaCreateRequest request) {
        if (request == null) throw new BusinessRuleException("Arıza oluşturma talebi zorunludur");
        String inventoryId = requiredId(envanterGuid, "envanterGuid");
        Envanter envanter = envanterRepository.findByGuidForUpdate(inventoryId)
            .orElseThrow(() -> new ResourceNotFoundException("Envanter bulunamadı: " + inventoryId));
        if (arizaRepository.existsOpenByEnvanterGuid(inventoryId)) {
            throw new BusinessRuleException("Bu envanter için zaten açık bir arıza kaydı bulunmaktadır.");
        }
        if (request.arizaAciklamasi() == null || request.arizaAciklamasi().isBlank()) {
            throw new BusinessRuleException("arizaAciklamasi zorunludur");
        }
        LocalDateTime faultDate = request.arizaTarihi() == null ? LocalDateTime.now() : request.arizaTarihi();
        if (faultDate.isAfter(LocalDateTime.now())) {
            throw new BusinessRuleException("Arıza tarihi gelecekte olamaz");
        }
        Ariza ariza = new Ariza();
        ariza.setGuid(UUID.randomUUID().toString());
        ariza.setEnvanter(envanter);
        ariza.setArizaTarihi(faultDate);
        ariza.setArizaAciklamasi(request.arizaAciklamasi().trim());
        ariza.setBildirimYapanPersonelKodu(trim(request.bildirimYapanPersonelKodu()));
        ariza.setAciklama(trim(request.aciklama()));
        ariza.setSonDurum(ArizaDurumTipi.ARIZA_BILDIRILDI);
        ariza.setKapandiMi(false);
        Ariza saved = arizaRepository.save(ariza);
        durumRepository.save(newStatus(saved, ArizaDurumTipi.ARIZA_BILDIRILDI, faultDate,
            "Arıza kaydı oluşturuldu"));
        return response(saved, ownershipService.resolveAll(List.of(saved)).get(saved.getGuid()));
    }

    @Override
    public PageResponse<ArizaResponse> search(ArizaFilter filter, Pageable pageable) {
        validateDates(filter);
        Pageable safe = PageableFactory.sanitize(pageable, DEFAULT_SORT, SORT_FIELDS);
        var page = arizaRepository.findAll(ArizaSpecifications.filtered(filter), safe);
        Map<String, ArizaSahiplikResponse> ownership = ownershipService.resolveAll(page.getContent());
        return PageResponse.of(page.getContent().stream()
            .map(a -> response(a, ownership.get(a.getGuid()))).toList(), page.getNumber(), page.getSize(),
            page.getTotalElements());
    }

    @Override
    public ArizaDetailResponse getByGuid(String arizaGuid) {
        Ariza ariza = find(arizaGuid);
        var ownership = ownershipService.resolveAll(List.of(ariza)).get(ariza.getGuid());
        return new ArizaDetailResponse(response(ariza, ownership), inventory(ariza.getEnvanter()),
            durumRepository.findHistory(ariza.getGuid()).stream().map(this::statusResponse).toList());
    }

    @Override
    @Transactional
    public ArizaDurumuResponse addStatus(String arizaGuid, ArizaDurumuCreateRequest request) {
        if (request == null) throw new BusinessRuleException("Arıza durumu talebi zorunludur");
        Ariza ariza = findForUpdate(arizaGuid);
        if (Boolean.TRUE.equals(ariza.getKapandiMi())) {
            throw new BusinessRuleException("Kapanmış arızaya yeni durum eklenemez");
        }
        ArizaTransitionValidator.validate(ariza.getSonDurum(), request.durumTipi());
        LocalDateTime statusDate = request.durumTarihi() == null ? LocalDateTime.now() : request.durumTarihi();
        LocalDateTime latestDate = durumRepository.findLatest(ariza.getGuid())
            .map(ArizaDurumu::getDurumTarihi).orElse(ariza.getArizaTarihi());
        if (statusDate.isBefore(latestDate)) {
            throw new BusinessRuleException("Durum tarihi önceki durum tarihinden önce olamaz");
        }
        validateMoney(request);
        ArizaDurumu status = newStatus(ariza, request.durumTipi(), statusDate, request.aciklama());
        status.setServisAdi(trim(request.servisAdi()));
        status.setServisReferansNo(trim(request.servisReferansNo()));
        status.setKargoFirmasi(trim(request.kargoFirmasi()));
        status.setKargoTakipNo(trim(request.kargoTakipNo()));
        status.setTamirSonucu(request.tamirSonucu());
        status.setTamirAciklamasi(trim(request.tamirAciklamasi()));
        status.setMasraf(request.masraf());
        status.setParaBirimi(request.paraBirimi() == null ? null : request.paraBirimi().toUpperCase(Locale.ROOT));
        ArizaDurumu saved = durumRepository.save(status);
        ariza.setSonDurum(request.durumTipi());
        arizaRepository.save(ariza);
        return statusResponse(saved);
    }

    @Override
    public List<ArizaDurumuResponse> getStatuses(String arizaGuid) {
        Ariza ariza = find(arizaGuid);
        return durumRepository.findHistory(ariza.getGuid()).stream().map(this::statusResponse).toList();
    }

    @Override
    @Transactional
    public ArizaResponse close(String arizaGuid, ArizaKapatRequest request) {
        Ariza ariza = findForUpdate(arizaGuid);
        if (Boolean.TRUE.equals(ariza.getKapandiMi())) {
            throw new BusinessRuleException("Arıza zaten kapatılmış");
        }
        LocalDateTime now = LocalDateTime.now();
        ArizaDurumu closed = newStatus(ariza, ArizaDurumTipi.KAPATILDI, now,
            request == null ? null : trim(request.aciklama()));
        durumRepository.save(closed);
        ariza.setSonDurum(ArizaDurumTipi.KAPATILDI);
        ariza.setKapandiMi(true);
        ariza.setKapanisTarihi(now);
        Ariza saved = arizaRepository.save(ariza);
        return response(saved, ownershipService.resolveAll(List.of(saved)).get(saved.getGuid()));
    }

    private Ariza find(String guid) {
        String id = requiredId(guid, "arizaGuid");
        return arizaRepository.findDetailByGuid(id)
            .orElseThrow(() -> new ResourceNotFoundException("Arıza bulunamadı: " + id));
    }

    private Ariza findForUpdate(String guid) {
        String id = requiredId(guid, "arizaGuid");
        return arizaRepository.findByGuidForUpdate(id)
            .orElseThrow(() -> new ResourceNotFoundException("Arıza bulunamadı: " + id));
    }

    private static ArizaDurumu newStatus(Ariza ariza, ArizaDurumTipi type,
                                         LocalDateTime date, String description) {
        ArizaDurumu status = new ArizaDurumu();
        status.setGuid(UUID.randomUUID().toString());
        status.setAriza(ariza);
        status.setDurumTipi(type);
        status.setDurumTarihi(date);
        status.setAciklama(trim(description));
        return status;
    }

    private ArizaResponse response(Ariza ariza, ArizaSahiplikResponse ownership) {
        return new ArizaResponse(ariza.getGuid(), ariza.getEnvanter().getGuid(), ariza.getArizaTarihi(),
            ariza.getArizaAciklamasi(), ariza.getBildirimYapanPersonelKodu(), ownership,
            ariza.getSonDurum(), Boolean.TRUE.equals(ariza.getKapandiMi()), ariza.getKapanisTarihi(),
            ariza.getAciklama());
    }

    private static ArizaEnvanterResponse inventory(Envanter e) {
        return new ArizaEnvanterResponse(e.getGuid(), e.getEnvanterAdi(), e.getEnvanterTuru(),
            e.getDemirbasNumarasi(), e.getSeriNo(), e.getBarkod());
    }

    private ArizaDurumuResponse statusResponse(ArizaDurumu d) {
        return new ArizaDurumuResponse(d.getGuid(), d.getAriza().getGuid(), d.getDurumTipi(),
            d.getDurumTarihi(), d.getAciklama(), d.getServisAdi(), d.getServisReferansNo(),
            d.getKargoFirmasi(), d.getKargoTakipNo(), d.getTamirSonucu(), d.getTamirAciklamasi(),
            d.getMasraf(), d.getParaBirimi());
    }

    private static void validateDates(ArizaFilter filter) {
        if (filter != null && filter.baslangicTarihi() != null && filter.bitisTarihi() != null
            && filter.bitisTarihi().isBefore(filter.baslangicTarihi())) {
            throw new BusinessRuleException("Bitiş tarihi başlangıç tarihinden önce olamaz");
        }
    }

    private static void validateMoney(ArizaDurumuCreateRequest request) {
        if (request.masraf() != null && (request.paraBirimi() == null || request.paraBirimi().isBlank())) {
            throw new BusinessRuleException("Masraf girildiğinde paraBirimi zorunludur");
        }
    }

    private static String requiredId(String value, String field) {
        if (value == null || value.isBlank()) throw new BusinessRuleException(field + " zorunludur");
        return value.trim();
    }

    private static String trim(String value) { return value == null ? null : value.trim(); }
}
