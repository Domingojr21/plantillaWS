package com.banreservas.dtos.inbound;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * DTO de respuesta principal que sigue los lineamientos de desarrollo.
 * Estructura estándar con header (responseCode y responseMessage) y body.
 * 
 * @author Sistema de Integración
 * @since 2025-07-22
 * @version 1.0
 */
@RegisterForReflection
public record ResponseDto(
       @JsonProperty("header") 
        ResponseHeaderDto header,
        @JsonProperty("body") 
        ProductsResponseDto body) implements Serializable {
}