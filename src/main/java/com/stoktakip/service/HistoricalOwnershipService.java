package com.stoktakip.service;

import com.stoktakip.dto.ArizaSahiplikResponse;
import com.stoktakip.entity.Ariza;
import com.stoktakip.entity.Depo;
import com.stoktakip.entity.DepoZimmetHareket;
import com.stoktakip.entity.PersonelZimmetHareket;
import com.stoktakip.enums.EnvanterSahiplikTipi;
import com.stoktakip.enums.ZimmetHareketTipi;
import com.stoktakip.exception.BusinessRuleException;
import com.stoktakip.repository.DepoZimmetHareketRepository;
import com.stoktakip.repository.PersonelZimmetHareketRepository;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HistoricalOwnershipService {
    private static final Logger log = LoggerFactory.getLogger(HistoricalOwnershipService.class);
    private static final Comparator<PersonelZimmetHareket> PERSON_SORT = Comparator
        .comparing(PersonelZimmetHareket::getOlusturmaTarihi).thenComparing(PersonelZimmetHareket::getId);
    private static final Comparator<DepoZimmetHareket> DEPO_SORT = Comparator
        .comparing(DepoZimmetHareket::getOlusturmaTarihi).thenComparing(DepoZimmetHareket::getId);

    private final PersonelZimmetHareketRepository personelRepository;
    private final DepoZimmetHareketRepository depoRepository;

    public HistoricalOwnershipService(PersonelZimmetHareketRepository personelRepository,
                                      DepoZimmetHareketRepository depoRepository) {
        this.personelRepository = personelRepository;
        this.depoRepository = depoRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, ArizaSahiplikResponse> resolveAll(Collection<Ariza> arizalar) {
        if (arizalar.isEmpty()) return Map.of();
        LocalDateTime maxDate = arizalar.stream().map(Ariza::getArizaTarihi).max(LocalDateTime::compareTo).orElseThrow();
        var inventoryIds = arizalar.stream().map(a -> a.getEnvanter().getGuid()).collect(Collectors.toSet());
        var people = personelRepository.findHistoricalEvents(inventoryIds, maxDate).stream()
            .collect(Collectors.groupingBy(h -> h.getEnvanter().getGuid()));
        var depots = depoRepository.findHistoricalEvents(inventoryIds, maxDate).stream()
            .collect(Collectors.groupingBy(h -> h.getEnvanter().getGuid()));
        return arizalar.stream().collect(Collectors.toMap(Ariza::getGuid,
            a -> resolve(a, people.getOrDefault(a.getEnvanter().getGuid(), List.of()),
                depots.getOrDefault(a.getEnvanter().getGuid(), List.of()))));
    }

    private ArizaSahiplikResponse resolve(Ariza ariza, List<PersonelZimmetHareket> personEvents,
                                          List<DepoZimmetHareket> depotEvents) {
        var relevantPersonEvents = personEvents.stream()
            .filter(h -> !h.getOlusturmaTarihi().isAfter(ariza.getArizaTarihi()))
            .sorted(PERSON_SORT).toList();
        var activePeople = new HashMap<String, Integer>();
        relevantPersonEvents.forEach(h -> {
                if (h.getHareketTipi() == ZimmetHareketTipi.PERSONELE_ZIMMET) {
                    activePeople.merge(h.getPersonelKodu(), 1, Integer::sum);
                } else if (h.getHareketTipi() == ZimmetHareketTipi.IADE) {
                    activePeople.computeIfPresent(h.getPersonelKodu(), (key, count) -> count == 1 ? null : count - 1);
                }
            });
        int activeAssignmentCount = activePeople.values().stream().mapToInt(Integer::intValue).sum();
        if (activeAssignmentCount > 1) {
            log.error("Arıza tarihinde çakışan personel zimmetleri: arizaGuid={}, envanterGuid={}, personeller={}",
                ariza.getGuid(), ariza.getEnvanter().getGuid(), activePeople);
            throw new BusinessRuleException("Arıza tarihinde birden fazla aktif personel zimmeti bulundu");
        }
        if (activeAssignmentCount == 1) {
            return new ArizaSahiplikResponse(EnvanterSahiplikTipi.PERSONEL,
                activePeople.keySet().iterator().next(), null, null, null);
        }
        DepoZimmetHareket lastDepotEvent = depotEvents.stream()
            .filter(h -> !h.getOlusturmaTarihi().isAfter(ariza.getArizaTarihi())).max(DEPO_SORT).orElse(null);
        LocalDateTime lastPersonEventDate = relevantPersonEvents.isEmpty() ? null
            : relevantPersonEvents.getLast().getOlusturmaTarihi();
        Depo depot = lastDepotEvent == null ? null : lastDepotEvent.getGidecegiDepo();
        if (lastPersonEventDate != null && (lastDepotEvent == null
            || lastDepotEvent.getOlusturmaTarihi().isBefore(lastPersonEventDate))) {
            depot = null;
        }
        if (depot != null) {
            return new ArizaSahiplikResponse(EnvanterSahiplikTipi.DEPO, null, null,
                depot.getGuid(), depot.getAd());
        }
        log.warn("Arıza tarihinde sahiplik bulunamadı: arizaGuid={}, envanterGuid={}, tarih={}",
            ariza.getGuid(), ariza.getEnvanter().getGuid(), ariza.getArizaTarihi());
        return ArizaSahiplikResponse.belirsiz();
    }
}
