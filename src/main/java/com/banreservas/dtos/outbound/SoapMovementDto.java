package com.banreservas.dtos.outbound;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * DTO para movimientos de préstamo desde el servicio SOAP.
 * Mapea únicamente los campos necesarios del XML de respuesta.
 * 
 * @author Consultor Domingo Ruiz - C-DJruiz@banreservas.com
 * @since 2025-07-22
 * @version 1.0
 */
@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true)
public record SoapMovementDto(
        @JsonProperty("NumeroTransaccion") 
        String transactionNumber,
        
        @JsonProperty("Fecha") 
        String date,
        
        @JsonProperty("MontoMovimiento") 
        String amount,
        
        @JsonProperty("Concepto") 
        String description,
        
        @JsonProperty("Causal") 
        String causal,
        
        @JsonProperty("IdUnico") 
        String uniqueId) {
}