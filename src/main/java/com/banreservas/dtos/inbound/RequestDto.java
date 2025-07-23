package com.banreservas.dtos.inbound;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotEmpty;

/**
 * DTO de entrada para el servicio de últimos movimientos de préstamo.
 * Contiene la información del producto (número de préstamo) para consultar
 * sus movimientos.
 * @author Consultor Domingo Ruiz - C-DJruiz@banreservas.com
 * @since 2025-07-21
 * @version 1.0
 */
@RegisterForReflection
public record RequestDto(
        @JsonProperty("productNumber") 
        @NotEmpty(message = "El número de producto es obligatorio") 
        String productNumber,
        @JsonProperty("productLine") 
        @NotEmpty(message = "La línea de producto es obligatoria") 
        String productLine,
        @JsonProperty("currency") 
        @NotEmpty(message = "La moneda es obligatoria") 
        String currency) implements Serializable {
}