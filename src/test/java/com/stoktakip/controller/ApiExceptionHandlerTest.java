package com.stoktakip.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void veriButunluguHatasiTeknikDetaySizDoner() {
        var response = handler.dataIntegrity();

        assertThat(response).containsEntry(
            "message", "İşlem veri bütünlüğü kuralları nedeniyle tamamlanamadı");
        assertThat(response.toString()).doesNotContain("constraint", "SQL");
    }
}
