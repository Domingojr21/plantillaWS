package com.banreservas.dtos.inbound;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * DTO que representa un movimiento individual de préstamo.
 * Contiene toda la información de una transacción específica
 * del producto consultado.
 * @author Consultor Domingo Ruiz - C-DJruiz@banreservas.com
 * @since 2025-07-21
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