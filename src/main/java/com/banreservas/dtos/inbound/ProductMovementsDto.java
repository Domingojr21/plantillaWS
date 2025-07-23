package com.banreservas.dtos.inbound;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * DTO que representa un producto con sus movimientos asociados.
 * Contiene la información del producto y la lista de todos sus movimientos
 * consultados en el rango de fechas especificado.
 * @author Sistema de Integración
 * @since 2025-01-28
 * @version 1.0
 */
@RegisterForReflection
public record ProductMovementsDto(
        @JsonProperty("productNumber") String productNumber,
        @JsonProperty("productLine") String productLine,
        @JsonProperty("currency") String currency,
        @JsonProperty("movements") List<MovementDto> movements,
        @JsonProperty("pagination") PaginationDto pagination) 
        implements Serializable {
}