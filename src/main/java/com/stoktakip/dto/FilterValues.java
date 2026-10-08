package com.stoktakip.dto;

import com.stoktakip.exception.BusinessRuleException;
import java.util.List;
import java.util.Objects;

/**
 * Çoklu değerli liste süzgeçlerinin ortak normalleştirmesi: null öğeler ve boş
 * metinler atılır, metinler kırpılır, yinelenenler tekilleştirilir. Sonuç hiçbir
 * zaman null değildir; boş liste "süzgeç yok" demektir.
 */
final class FilterValues {
    /** Oracle IN listesi 1000 öğeyle sınırlı (ORA-01795); istek başına makul üst sınır. */
    static final int MAX_VALUES = 100;

    private FilterValues() {
    }

    static List<String> texts(String name, List<String> values) {
        if (values == null) {
            return List.of();
        }
        return limited(name, values.stream()
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(value -> !value.isEmpty())
            .distinct()
            .toList());
    }

    static <E extends Enum<E>> List<E> enums(String name, List<E> values) {
        if (values == null) {
            return List.of();
        }
        return limited(name, values.stream().filter(Objects::nonNull).distinct().toList());
    }

    private static <T> List<T> limited(String name, List<T> values) {
        if (values.size() > MAX_VALUES) {
            throw new BusinessRuleException(
                name + " için en fazla " + MAX_VALUES + " değer gönderilebilir");
        }
        return values;
    }
}
