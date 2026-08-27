package com.stoktakip.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void veriButunluguHatasiTeknikDetaySizDoner() {
        var response = handler.dataIntegrity();

        assertThat(response).containsEntry(
            "message", "İşlem veri bütünlüğü kuralları nedeniyle tamamlanamadı");
        assertThat(response.toString()).doesNotContain("constraint", "SQL");
    }

    @Test
    void gecersizQueryParametresiTutarlıHataGovdesiDoner() {
        var cause = new ConversionFailedException(
            TypeDescriptor.valueOf(String.class), TypeDescriptor.valueOf(Long.class),
            "abc", new NumberFormatException("For input string: abc"));
        var exception = new MethodArgumentTypeMismatchException(
            "abc", Long.class, "demirbasNumarasi", null, cause);

        assertThat(handler.requestParameterType(exception))
            .containsExactlyEntriesOf(java.util.Map.of(
                "message", "demirbasNumarasi: geçersiz değer"));
    }
}
