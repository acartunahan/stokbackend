package com.stoktakip.repository;

import com.stoktakip.dto.ArizaFilter;
import com.stoktakip.entity.Ariza;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import org.springframework.data.jpa.domain.Specification;

public final class ArizaSpecifications {
    private ArizaSpecifications() {}

    public static Specification<Ariza> filtered(ArizaFilter filter) {
        return (root, query, cb) -> {
            var predicates = new ArrayList<Predicate>();
            predicates.add(cb.isFalse(root.get("silindi")));
            predicates.add(cb.isFalse(root.get("envanter").get("silindi")));
            if (filter == null) return cb.and(predicates.toArray(Predicate[]::new));
            if (hasText(filter.envanterGuid())) {
                predicates.add(cb.equal(root.get("envanter").get("guid"), filter.envanterGuid().trim()));
            }
            if (filter.durumTipi() != null) predicates.add(cb.equal(root.get("sonDurum"), filter.durumTipi()));
            if (filter.kapandiMi() != null) predicates.add(cb.equal(root.get("kapandiMi"), filter.kapandiMi()));
            if (filter.baslangicTarihi() != null) predicates.add(cb.greaterThanOrEqualTo(root.get("arizaTarihi"), filter.baslangicTarihi()));
            if (filter.bitisTarihi() != null) predicates.add(cb.lessThanOrEqualTo(root.get("arizaTarihi"), filter.bitisTarihi()));
            if (hasText(filter.q())) {
                String q = "%" + filter.q().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("arizaAciklamasi")), q),
                    cb.like(cb.lower(root.get("aciklama")), q),
                    cb.like(cb.lower(root.get("envanter").get("envanterAdi")), q)
                ));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static boolean hasText(String value) { return value != null && !value.isBlank(); }
}
