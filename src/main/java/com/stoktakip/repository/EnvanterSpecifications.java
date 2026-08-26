package com.stoktakip.repository;

import com.stoktakip.dto.EnvanterFilter;
import com.stoktakip.entity.Envanter;
import jakarta.persistence.criteria.JoinType;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class EnvanterSpecifications {
    private EnvanterSpecifications() {
    }

    public static Specification<Envanter> filtered(EnvanterFilter filter) {
        return (root, query, builder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            predicates.add(builder.isFalse(root.get("silindi")));
            if (filter == null) {
                return builder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
            }
            equalGuid(builder, predicates, root.join("model", JoinType.LEFT)
                .join("kategori", JoinType.LEFT).get("guid"), filter.kategoriGuid());
            equalGuid(builder, predicates, root.join("model", JoinType.LEFT)
                .join("marka", JoinType.LEFT).get("guid"), filter.markaGuid());
            equalGuid(builder, predicates, root.join("model", JoinType.LEFT).get("guid"),
                filter.modelGuid());
            equalGuid(builder, predicates, root.join("depo", JoinType.LEFT).get("guid"),
                filter.depoGuid());
            equalGuid(builder, predicates, root.join("detay", JoinType.LEFT)
                .join("firma", JoinType.LEFT).get("guid"), filter.firmaGuid());
            if (filter.envanterTuru() != null) {
                predicates.add(builder.equal(root.get("envanterTuru"), filter.envanterTuru()));
            }
            equalIgnoreCase(builder, predicates, root.get("seriNo"), filter.seriNo());
            equalIgnoreCase(builder, predicates, root.get("barkod"), filter.barkod());
            if (filter.demirbasNumarasi() != null) {
                predicates.add(builder.equal(root.get("demirbasNumarasi"),
                    filter.demirbasNumarasi()));
            }
            if (hasText(filter.q())) {
                var pattern = builder.lower(builder.literal(
                    "%" + escapeLike(filter.q().trim()) + "%"));
                predicates.add(builder.or(
                    builder.like(builder.lower(root.get("envanterAdi")), pattern, '\\'),
                    builder.like(builder.lower(root.get("seriNo")), pattern, '\\'),
                    builder.like(builder.lower(root.get("barkod")), pattern, '\\'),
                    builder.like(builder.lower(root.get("aciklama")), pattern, '\\')
                ));
            }
            query.distinct(true);
            return builder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }

    private static void equalGuid(
        jakarta.persistence.criteria.CriteriaBuilder builder,
        List<jakarta.persistence.criteria.Predicate> predicates,
        jakarta.persistence.criteria.Path<String> path,
        String value
    ) {
        if (hasText(value)) {
            predicates.add(builder.equal(path, value.trim()));
        }
    }

    private static void equalIgnoreCase(
        jakarta.persistence.criteria.CriteriaBuilder builder,
        List<jakarta.persistence.criteria.Predicate> predicates,
        jakarta.persistence.criteria.Path<String> path,
        String value
    ) {
        if (hasText(value)) {
            predicates.add(builder.equal(
                builder.lower(path), builder.lower(builder.literal(value.trim()))));
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
