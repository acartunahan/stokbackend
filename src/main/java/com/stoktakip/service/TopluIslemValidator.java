package com.stoktakip.service;

import com.stoktakip.dto.TopluEnvanterItemRequest;
import com.stoktakip.exception.BusinessRuleException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class TopluIslemValidator {
    private TopluIslemValidator() {
    }

    public static List<TopluEnvanterItemRequest> validateAndSort(
        List<TopluEnvanterItemRequest> items
    ) {
        if (items == null || items.isEmpty()) {
            throw new BusinessRuleException("envanterler boş olamaz");
        }
        Set<String> uniqueIds = new HashSet<>();
        for (TopluEnvanterItemRequest item : items) {
            if (item == null || item.envanterGuid() == null || item.envanterGuid().isBlank()) {
                throw new BusinessRuleException("envanterGuid zorunludur");
            }
            if (item.adet() == null || item.adet() != 1L) {
                throw new BusinessRuleException(
                    "Mevcut envanter modeli kayıt bazlıdır; her kalem için adet 1 olmalıdır");
            }
            String guid = item.envanterGuid().trim();
            if (!uniqueIds.add(guid)) {
                throw new BusinessRuleException("Aynı envanter toplu istekte birden fazla kez kullanılamaz: " + guid);
            }
        }
        return items.stream()
            .map(item -> new TopluEnvanterItemRequest(item.envanterGuid().trim(), item.adet()))
            .sorted(Comparator.comparing(TopluEnvanterItemRequest::envanterGuid))
            .toList();
    }
}
