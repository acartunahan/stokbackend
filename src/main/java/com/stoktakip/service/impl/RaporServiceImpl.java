package com.stoktakip.service.impl;

import com.stoktakip.dto.GarantiBitenResponse;
import com.stoktakip.dto.RaporOzetResponse;
import com.stoktakip.entity.Envanter;
import com.stoktakip.entity.EnvanterDetay;
import com.stoktakip.enums.EnvanterTuru;
import com.stoktakip.exception.BusinessRuleException;
import com.stoktakip.repository.ArizaRepository;
import com.stoktakip.repository.DepoRepository;
import com.stoktakip.repository.EnvanterDetayRepository;
import com.stoktakip.repository.EnvanterRepository;
import com.stoktakip.service.RaporService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RaporServiceImpl implements RaporService {
    private static final int MAX_GUN = 3650;

    private final EnvanterRepository envanterRepository;
    private final EnvanterDetayRepository detayRepository;
    private final DepoRepository depoRepository;
    private final ArizaRepository arizaRepository;

    public RaporServiceImpl(
        EnvanterRepository envanterRepository, EnvanterDetayRepository detayRepository,
        DepoRepository depoRepository, ArizaRepository arizaRepository
    ) {
        this.envanterRepository = envanterRepository;
        this.detayRepository = detayRepository;
        this.depoRepository = depoRepository;
        this.arizaRepository = arizaRepository;
    }

    @Override
    public RaporOzetResponse getOzet() {
        long demirbas = envanterRepository.countAktifByTur(EnvanterTuru.DEMIRBAS);
        long sarf = envanterRepository.countAktifByTur(EnvanterTuru.SARF);
        return new RaporOzetResponse(envanterRepository.count(), demirbas, sarf,
            depoRepository.count(), arizaRepository.countAcik(), garantisiBitecekler(30).size());
    }

    @Override
    public List<GarantiBitenResponse> getGarantisiBitecekler(int gun) {
        if (gun < 0 || gun > MAX_GUN) {
            throw new BusinessRuleException("gun 0 ile " + MAX_GUN + " arasında olmalıdır");
        }
        LocalDate bugun = LocalDate.now();
        return garantisiBitecekler(gun).stream().map(detay -> toResponse(detay, bugun)).toList();
    }

    private List<EnvanterDetay> garantisiBitecekler(int gun) {
        LocalDate bugun = LocalDate.now();
        return detayRepository.findGarantisiBitecekler(bugun.atStartOfDay(),
            bugun.plusDays(gun).atTime(23, 59, 59));
    }

    private static GarantiBitenResponse toResponse(EnvanterDetay detay, LocalDate bugun) {
        Envanter envanter = detay.getEnvanter();
        LocalDateTime bitis = detay.getGarantiBitisTarihi();
        return new GarantiBitenResponse(envanter.getGuid(), envanter.getEnvanterAdi(),
            envanter.getDemirbasNumarasi(), envanter.getSeriNo(),
            envanter.getDepo() == null ? null : envanter.getDepo().getGuid(),
            envanter.getDepo() == null ? null : envanter.getDepo().getAd(),
            bitis, ChronoUnit.DAYS.between(bugun, bitis.toLocalDate()));
    }
}
