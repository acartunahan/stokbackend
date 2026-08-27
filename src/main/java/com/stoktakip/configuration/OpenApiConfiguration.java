package com.stoktakip.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.models.Operation;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Stok Takip API",
        version = "v1",
        description = "Envanter, depo, katalog ve zimmet süreçlerini yöneten REST API dokümantasyonu.",
        contact = @Contact(name = "Stok Takip Backend Ekibi"),
        license = @License(name = "Kurum İçi Kullanım")
    )
)
public class OpenApiConfiguration {

    @Bean
    OpenApiCustomizer inventoryAliasOperationIds() {
        return openApi -> {
            setGetId(openApi, "/api/envanterler", "listInventories");
            setPostId(openApi, "/api/envanterler", "createInventory");
            setGetId(openApi, "/api/envanterler/lookup", "lookupInventoryByIdentifier");
            setGetId(openApi, "/api/envanterler/{guid}/hareketler", "getInventoryMovementHistory");
            setGetId(openApi, "/api/envanterler/sarf", "listConsumableInventories");
            setGetId(openApi, "/api/envanterler/demirbas", "listFixedAssetInventories");
            setGetId(openApi, "/api/envanterler/{guid}", "getInventoryDetail");
            setPutId(openApi, "/api/envanterler/{guid}", "updateInventory");
            setDeleteId(openApi, "/api/envanterler/{guid}", "deleteInventory");
            setGetId(openApi, "/api/envanterler/{envanterGuid}/zimmet-durumu",
                "getInventoryAssignmentStatus");

            setGetId(openApi, "/api/envanter", "listInventoriesLegacy");
            setPostId(openApi, "/api/envanter", "createInventoryLegacy");
            setGetId(openApi, "/api/envanter/lookup", "lookupInventoryByIdentifierLegacy");
            setGetId(openApi, "/api/envanter/{guid}/hareketler",
                "getInventoryMovementHistoryLegacy");
            setGetId(openApi, "/api/envanter/sarf", "listConsumableInventoriesLegacy");
            setGetId(openApi, "/api/envanter/demirbas", "listFixedAssetInventoriesLegacy");
            setGetId(openApi, "/api/envanter/{guid}", "getInventoryDetailLegacy");
            setPutId(openApi, "/api/envanter/{guid}", "updateInventoryLegacy");
            setDeleteId(openApi, "/api/envanter/{guid}", "deleteInventoryLegacy");
            setGetId(openApi, "/api/envanter/{envanterGuid}/zimmet-durumu",
                "getInventoryAssignmentStatusLegacy");
        };
    }

    private static void setGetId(io.swagger.v3.oas.models.OpenAPI openApi, String path, String id) {
        setId(openApi.getPaths().get(path).getGet(), id);
    }

    private static void setPostId(io.swagger.v3.oas.models.OpenAPI openApi, String path, String id) {
        setId(openApi.getPaths().get(path).getPost(), id);
    }

    private static void setPutId(io.swagger.v3.oas.models.OpenAPI openApi, String path, String id) {
        setId(openApi.getPaths().get(path).getPut(), id);
    }

    private static void setDeleteId(io.swagger.v3.oas.models.OpenAPI openApi, String path, String id) {
        setId(openApi.getPaths().get(path).getDelete(), id);
    }

    private static void setId(Operation operation, String id) {
        operation.setOperationId(id);
    }
}
