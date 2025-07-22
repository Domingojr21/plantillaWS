package com.banreservas.dtos.inbound;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * DTO que representa un movimiento individual de préstamo.
 * Contiene toda la información de una transacción específica
 * del producto consultado.
 * 
 * Este DTO mapea la información que viene del backend SOAP
 * y se estructura según la respuesta del servicio MovimientosPrestamo.
 * 
 * @author Sistema de Integración
 * @since 2025-01-28
 * @version 1.0
 */
@RegisterForReflection
public record MovementDto(
        @JsonProperty("currency") String currency,
        @JsonProperty("amount") BigDecimal amount,
        @JsonProperty("date") String date,
        @JsonProperty("description") String description,
        @JsonProperty("status") String status,
        @JsonProperty("transactionNumber") String transactionNumber,
        @JsonProperty("uniqueId") String uniqueId,
        @JsonProperty("causal") String causal) 
        implements Serializable {
}