package com.banreservas.dtos.inbound;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * DTO que maneja la información de paginación para los movimientos.
 * Contiene el identificador único utilizado para el control de paginación
 * en consultas de movimientos.
 * @author Sistema de Integración
 * @since 2025-01-28
 * @version 1.0
 */
@RegisterForReflection
public record PaginationDto(
        @JsonProperty("uniqueId") String uniqueId) 
        implements Serializable {
}