package com.stoktakip.service.impl;

import com.stoktakip.dto.EnvanterResponse;
import com.stoktakip.dto.EnvanterCreateRequest;
import com.stoktakip.dto.EnvanterDetayCreateRequest;
import com.stoktakip.dto.EnvanterDetayResponse;
import com.stoktakip.dto.EnvanterUpdateRequest;
import com.stoktakip.dto.EnvanterZimmetDurumuResponse;
import com.stoktakip.dto.EnvanterFilter;
import com.stoktakip.dto.PageResponse;
import com.stoktakip.entity.Depo;
import com.stoktakip.entity.DepoZimmetHareket;
import com.stoktakip.entity.Envanter;
import com.stoktakip.entity.EnvanterDetay;
import com.stoktakip.entity.Firma;
import com.stoktakip.entity.Ihale;
import com.stoktakip.entity.Model;
import com.stoktakip.entity.PersonelZimmetHareket;
import com.stoktakip.enums.EnvanterTuru;
import com.stoktakip.enums.ZimmetHareketTipi;
import com.stoktakip.exception.BusinessRuleException;
import com.stoktakip.exception.ResourceNotFoundException;
import com.stoktakip.repository.EnvanterRepository;
import com.stoktakip.repository.EnvanterSpecifications;
import com.stoktakip.repository.EnvanterDetayRepository;
import com.stoktakip.repository.DepoRepository;
import com.stoktakip.repository.DepoZimmetHareketRepository;
import com.stoktakip.repository.ModelRepository;
import com.stoktakip.repository.FirmaRepository;
import com.stoktakip.repository.IhaleRepository;
import com.stoktakip.repository.PersonelZimmetHareketRepository;
import com.stoktakip.service.EnvanterService;
import com.stoktakip.service.PageableFactory;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EnvanterServiceImpl implements EnvanterService {
    private static final Set<String> SORT_FIELDS = Set.of(
        "guid", "envanterAdi", "envanterTuru", "demirbasNumarasi", "seriNo", "barkod", "sube"
    );
    private final EnvanterRepository repository;
    private final PersonelZimmetHareketRepository hareketRepository;
    private final DepoRepository depoRepository;
    private final ModelRepository modelRepository;
    private final EnvanterDetayRepository detayRepository;
    private final FirmaRepository firmaRepository;
    private final IhaleRepository ihaleRepository;
    private final DepoZimmetHareketRepository depoHareketRepository;

    public EnvanterServiceImpl(
        EnvanterRepository repository,
        PersonelZimmetHareketRepository hareketRepository,
        DepoRepository depoRepository,
        ModelRepository modelRepository,
        EnvanterDetayRepository detayRepository,
        FirmaRepository firmaRepository,
        IhaleRepository ihaleRepository,
        DepoZimmetHareketRepository depoHareketRepository
    ) {
        this.repository = repository;
        this.hareketRepository = hareketRepository;
        this.depoRepository = depoRepository;
        this.modelRepository = modelRepository;
        this.detayRepository = detayRepository;
        this.firmaRepository = firmaRepository;
        this.ihaleRepository = ihaleRepository;
        this.depoHareketRepository = depoHareketRepository;
    }

    @Override
    public List<EnvanterResponse> getAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public PageResponse<EnvanterResponse> search(EnvanterFilter filter, Pageable pageable) {
        Pageable safePageable = PageableFactory.sanitize(
            pageable, Sort.by("envanterAdi").ascending(), SORT_FIELDS);
        Page<EnvanterResponse> result = repository.findAll(
            EnvanterSpecifications.filtered(filter), safePageable
        ).map(this::toResponse);
        return PageResponse.from(result);
    }

    @Override
    public PageResponse<EnvanterResponse> getByDepo(
        String depoGuid, EnvanterFilter filter, Pageable pageable
    ) {
        String guid = normalizeGuid(depoGuid);
        depoRepository.findById(guid)
            .orElseThrow(() -> new ResourceNotFoundException("Depo bulunamadı: " + guid));
        return search(EnvanterFilter.depoIle(filter, guid), pageable);
    }

    @Override
    public EnvanterResponse lookup(String seriNo, Long demirbasNumarasi, String barkod) {
        int criteriaCount = (hasText(seriNo) ? 1 : 0)
            + (demirbasNumarasi == null ? 0 : 1)
            + (hasText(barkod) ? 1 : 0);
        if (criteriaCount != 1) {
            throw new BusinessRuleException(
                "seriNo, demirbasNumarasi veya barkod alanlarından tam olarak biri gönderilmelidir");
        }
        EnvanterFilter filter = new EnvanterFilter(
            null, null, null, null, null, null, seriNo, demirbasNumarasi, barkod, null);
        Page<Envanter> matches = repository.findAll(
            EnvanterSpecifications.filtered(filter),
            PageRequest.of(0, 2));
        if (matches.getTotalElements() == 0) {
            throw new ResourceNotFoundException("Envanter bulunamadı");
        }
        if (matches.getTotalElements() > 1) {
            throw new BusinessRuleException("Arama kriteri birden fazla aktif envanterle eşleşti");
        }
        return toResponse(matches.getContent().getFirst());
    }

    @Override
    public List<EnvanterResponse> getSarflar() {
        return getByType(EnvanterTuru.SARF);
    }

    @Override
    public List<EnvanterResponse> getDemirbaslar() {
        return getByType(EnvanterTuru.DEMIRBAS);
    }

    @Override
    public EnvanterResponse getById(String guid) {
        String normalizedGuid = normalizeGuid(guid);
        return toResponse(repository.findDetayByGuid(normalizedGuid)
            .orElseThrow(() -> new ResourceNotFoundException("Envanter bulunamadı: " + normalizedGuid)));
    }

    @Override
    @Transactional
    public EnvanterResponse create(EnvanterCreateRequest request) {
        if (request == null) {
            throw new BusinessRuleException("Envanter oluşturma talebi zorunludur");
        }
        Depo depo = depo(request.depoGuid());
        Model model = model(request.modelGuid());
        DetayReferanslari detayReferanslari = detayReferanslariniDogrula(request.detay());

        Envanter envanter = new Envanter();
        envanter.setGuid(UUID.randomUUID().toString());
        metadataGuncelle(envanter, request.envanterAdi(), request.envanterTuru(), model,
            request.demirbasNumarasi(), request.seriNo(), request.barkod(), request.sube(),
            request.aciklama(), request.ilSubeDaireBaskanlikNumarasi());
        envanter.setDepo(depo);
        Envanter kaydedilen = repository.save(envanter);

        EnvanterDetay detay = yeniDetay(request.detay(), detayReferanslari, kaydedilen);
        EnvanterDetay kaydedilenDetay = detayRepository.save(detay);
        kaydedilen.setDetay(kaydedilenDetay);

        DepoZimmetHareket ilkHareket = DepoZimmetHareket.yeni(
            kaydedilen, ZimmetHareketTipi.DEPO_TRANSFERI, "GIRIS", 1L, null, depo);
        ilkHareket.setAciklama("İlk depo girişi");
        depoHareketRepository.save(ilkHareket);
        return toResponse(kaydedilen);
    }

    @Override
    @Transactional
    public EnvanterResponse update(String guid, EnvanterUpdateRequest request) {
        if (request == null) {
            throw new BusinessRuleException("Envanter güncelleme talebi zorunludur");
        }
        String normalizedGuid = normalizeGuid(guid);
        Envanter envanter = repository.findById(normalizedGuid)
            .orElseThrow(() -> new ResourceNotFoundException("Envanter bulunamadı: " + normalizedGuid));
        metadataGuncelle(envanter, request.envanterAdi(), request.envanterTuru(), model(request.modelGuid()),
            request.demirbasNumarasi(), request.seriNo(), request.barkod(), request.sube(),
            request.aciklama(), request.ilSubeDaireBaskanlikNumarasi());
        return toResponse(repository.save(envanter));
    }

    @Override
    @Transactional
    public void delete(String guid) {
        String normalizedGuid = normalizeGuid(guid);
        Envanter envanter = repository.findById(normalizedGuid)
            .orElseThrow(() -> new ResourceNotFoundException("Envanter bulunamadı: " + normalizedGuid));
        if (hareketRepository.existsByEnvanterGuid(normalizedGuid)
            || depoHareketRepository.existsByEnvanterGuid(normalizedGuid)) {
            throw new BusinessRuleException("Hareket geçmişi bulunan envanter silinemez");
        }
        repository.delete(envanter);
    }

    @Override
    public EnvanterZimmetDurumuResponse getZimmetDurumu(String envanterGuid) {
        String guid = normalizeGuid(envanterGuid);
        Envanter envanter = repository.findDetayByGuid(guid)
            .orElseThrow(() -> new ResourceNotFoundException("Envanter bulunamadı: " + guid));

        return hareketRepository.findFirstByEnvanterGuidOrderByIdDesc(guid)
            .filter(this::aktifPersonelZimmeti)
            .map(hareket -> zimmetliResponse(envanter, hareket))
            .orElseGet(() -> zimmetsizResponse(envanter));
    }

    private List<EnvanterResponse> getByType(EnvanterTuru type) {
        return repository.findByEnvanterTuru(type).stream().map(this::toResponse).toList();
    }

    private EnvanterResponse toResponse(Envanter entity) {
        String depoGuid = entity.getDepo() == null ? null : entity.getDepo().getGuid();
        String modelGuid = entity.getModel() == null ? null : entity.getModel().getGuid();
        EnvanterDetayResponse detay = toDetayResponse(entity.getDetay());
        return new EnvanterResponse(entity.getGuid(), entity.getEnvanterAdi(),
            entity.getEnvanterTuru(), depoGuid, modelGuid, entity.getDemirbasNumarasi(),
            entity.getSeriNo(), entity.getBarkod(), entity.getSube(), entity.getAciklama(),
            entity.getIlSubeDaireBaskanlikNumarasi(), detay);
    }

    private void metadataGuncelle(
        Envanter envanter, String ad, EnvanterTuru tur, Model model,
        Long demirbasNo, String seriNo, String barkod, Long sube, String aciklama, String birimNo
    ) {
        if (ad == null || ad.isBlank()) {
            throw new BusinessRuleException("envanterAdi zorunludur");
        }
        if (tur == null) {
            throw new BusinessRuleException("envanterTuru zorunludur");
        }
        envanter.setEnvanterAdi(ad.trim());
        envanter.setEnvanterTuru(tur);
        envanter.setModel(model);
        envanter.setDemirbasNumarasi(demirbasNo);
        envanter.setSeriNo(seriNo);
        envanter.setBarkod(barkodDogrula(envanter, barkod));
        envanter.setSube(sube);
        envanter.setAciklama(aciklama);
        envanter.setIlSubeDaireBaskanlikNumarasi(birimNo);
    }

    // Eski sistemden aynı barkodla gelmiş kayıtlar, barkodu değiştirilmediği sürece güncellenebilir.
    private String barkodDogrula(Envanter envanter, String barkod) {
        String yeniBarkod = hasText(barkod) ? barkod.trim() : null;
        if (yeniBarkod == null || yeniBarkod.equalsIgnoreCase(envanter.getBarkod())) {
            return yeniBarkod;
        }
        if (repository.existsAktifBarkod(yeniBarkod, envanter.getGuid())) {
            throw new BusinessRuleException("Bu barkod başka bir envanterde kullanılıyor: " + yeniBarkod);
        }
        return yeniBarkod;
    }

    private Depo depo(String guid) {
        if (guid == null || guid.isBlank()) {
            throw new BusinessRuleException("depoGuid zorunludur");
        }
        String normalized = guid.trim();
        return depoRepository.findById(normalized)
            .orElseThrow(() -> new ResourceNotFoundException("Depo bulunamadı: " + normalized));
    }

    private Model model(String guid) {
        if (guid == null || guid.isBlank()) {
            return null;
        }
        String normalized = guid.trim();
        return modelRepository.findById(normalized)
            .orElseThrow(() -> new ResourceNotFoundException("Model bulunamadı: " + normalized));
    }

    private DetayReferanslari detayReferanslariniDogrula(EnvanterDetayCreateRequest request) {
        if (request == null) {
            return new DetayReferanslari(null, null);
        }
        garantiTarihleriniDogrula(request);

        Ihale ihale = ihale(request.ihaleGuid());
        Firma firma = firma(request.firmaGuid());
        if (ihale != null) {
            Firma ihaleFirmasi = ihale.getFirma();
            if (ihaleFirmasi == null) {
                throw new BusinessRuleException("İhalenin bağlı firması bulunamadı");
            }
            if (firma != null && !ihaleFirmasi.getGuid().equals(firma.getGuid())) {
                throw new BusinessRuleException("İhale ve firma birbiriyle uyuşmuyor");
            }
            firma = ihaleFirmasi;
        }
        return new DetayReferanslari(ihale, firma);
    }

    private EnvanterDetay yeniDetay(
        EnvanterDetayCreateRequest request,
        DetayReferanslari referanslar,
        Envanter envanter
    ) {
        EnvanterDetay detay = new EnvanterDetay();
        detay.setGuid(UUID.randomUUID().toString());
        detay.setEnvanter(envanter);
        detay.setIhale(referanslar.ihale());
        detay.setFirma(referanslar.firma());
        if (request != null) {
            detay.setAlimYolu(request.alimYolu());
            detay.setAlimTuru(request.alimTuru());
            detay.setSaymanlikNo(request.saymanlikNo());
            detay.setGarantiBaslangicTarihi(request.garantiBaslangicTarihi());
            detay.setGarantiBitisTarihi(request.garantiBitisTarihi());
        }
        return detay;
    }

    private Ihale ihale(String guid) {
        String normalized = normalizeOptionalGuid(guid);
        if (normalized == null) {
            return null;
        }
        return ihaleRepository.findDetayByGuid(normalized)
            .orElseThrow(() -> new ResourceNotFoundException("İhale bulunamadı: " + normalized));
    }

    private Firma firma(String guid) {
        String normalized = normalizeOptionalGuid(guid);
        if (normalized == null) {
            return null;
        }
        return firmaRepository.findById(normalized)
            .orElseThrow(() -> new ResourceNotFoundException("Firma bulunamadı: " + normalized));
    }

    private static void garantiTarihleriniDogrula(EnvanterDetayCreateRequest request) {
        if (request.garantiBaslangicTarihi() != null
            && request.garantiBitisTarihi() != null
            && request.garantiBitisTarihi().isBefore(request.garantiBaslangicTarihi())) {
            throw new BusinessRuleException(
                "Garanti bitiş tarihi başlangıç tarihinden önce olamaz");
        }
    }

    private static EnvanterDetayResponse toDetayResponse(EnvanterDetay detay) {
        if (detay == null) {
            return null;
        }
        return new EnvanterDetayResponse(
            detay.getGuid(),
            detay.getEnvanter().getGuid(),
            detay.getAlimYolu(),
            detay.getAlimTuru(),
            detay.getSaymanlikNo(),
            detay.getIhale() == null ? null : detay.getIhale().getGuid(),
            detay.getFirma() == null ? null : detay.getFirma().getGuid(),
            detay.getGarantiBaslangicTarihi(),
            detay.getGarantiBitisTarihi()
        );
    }

    private static String normalizeOptionalGuid(String guid) {
        return guid == null || guid.isBlank() ? null : guid.trim();
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private record DetayReferanslari(Ihale ihale, Firma firma) {
    }

    private boolean aktifPersonelZimmeti(PersonelZimmetHareket hareket) {
        return hareket.getHareketTipi() == ZimmetHareketTipi.PERSONELE_ZIMMET
            && Boolean.TRUE.equals(hareket.getAktif());
    }

    private EnvanterZimmetDurumuResponse zimmetliResponse(
        Envanter envanter,
        PersonelZimmetHareket hareket
    ) {
        return new EnvanterZimmetDurumuResponse(
            envanter.getGuid(), envanter.getEnvanterAdi(), true,
            hareket.getPersonelKodu(), hareket.getId(), hareket.getOlusturmaTarihi());
    }

    private EnvanterZimmetDurumuResponse zimmetsizResponse(Envanter envanter) {
        return new EnvanterZimmetDurumuResponse(
            envanter.getGuid(), envanter.getEnvanterAdi(), false, null, null, null);
    }

    private static String normalizeGuid(String envanterGuid) {
        if (envanterGuid == null || envanterGuid.isBlank()) {
            throw new BusinessRuleException("envanterGuid boş olamaz");
        }
        return envanterGuid.trim();
    }
}
