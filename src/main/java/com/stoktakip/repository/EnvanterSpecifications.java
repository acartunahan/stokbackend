package com.stoktakip.repository;

import com.stoktakip.dto.EnvanterFilter;
import com.stoktakip.entity.Envanter;
import com.stoktakip.entity.EnvanterDetay;
import com.stoktakip.entity.Model;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.data.jpa.domain.Specification;

public final class EnvanterSpecifications {
    private EnvanterSpecifications() {
    }

    public static Specification<Envanter> filtered(EnvanterFilter filter) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(builder.isFalse(root.get("silindi")));
            if (filter == null) {
                return builder.and(predicates.toArray(Predicate[]::new));
            }
            Joins joins = new Joins(root);
            in(predicates, () -> joins.model().join("kategori", JoinType.LEFT).get("guid"),
                filter.kategoriGuid());
            in(predicates, () -> joins.model().join("marka", JoinType.LEFT).get("guid"),
                filter.markaGuid());
            in(predicates, () -> joins.model().get("guid"), filter.modelGuid());
            in(predicates, () -> root.join("depo", JoinType.LEFT).get("guid"),
                filter.depoGuid());
            in(predicates, () -> joins.detay().join("firma", JoinType.LEFT).get("guid"),
                filter.firmaGuid());
            in(predicates, () -> root.get("envanterTuru"), filter.envanterTuru());
            if (filter.garantiBitisBaslangic() != null) {
                predicates.add(builder.greaterThanOrEqualTo(
                    joins.detay().get("garantiBitisTarihi"), filter.garantiBitisBaslangic()));
            }
            if (filter.garantiBitisBitis() != null) {
                predicates.add(builder.lessThanOrEqualTo(
                    joins.detay().get("garantiBitisTarihi"), filter.garantiBitisBitis()));
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
            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }

    /** İlişki join'lerini yalnız gerektiğinde, bir kez kurar ve yeniden kullanır. */
    private static final class Joins {
        private final Root<Envanter> root;
        private Join<Envanter, Model> model;
        private Join<Envanter, EnvanterDetay> detay;

        private Joins(Root<Envanter> root) {
            this.root = root;
        }

        Join<Envanter, Model> model() {
            if (model == null) {
                model = root.join("model", JoinType.LEFT);
            }
            return model;
        }

        Join<Envanter, EnvanterDetay> detay() {
            if (detay == null) {
                detay = root.join("detay", JoinType.LEFT);
            }
            return detay;
        }
    }

    /** Değerler {@link EnvanterFilter} kurucusunda normalleştirilir; boş liste = süzgeç yok. */
    private static void in(
        List<Predicate> predicates, Supplier<Path<?>> path, Collection<?> values
    ) {
        if (values != null && !values.isEmpty()) {
            predicates.add(path.get().in(values));
        }
    }

    private static void equalIgnoreCase(
        CriteriaBuilder builder,
        List<Predicate> predicates,
        Path<String> path,
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
