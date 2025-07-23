package com.banreservas.dtos.outbound;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * DTO para movimientos de préstamo desde el servicio SOAP.
 * Contiene solo los campos necesarios según el mapeo ESQL.
 * 
 * @author Sistema de Integración
 * @since 2025-07-22
 * @version 1.0
 */
@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true) // Ignora todos los demás campos
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
    
    /**
     * Obtiene la moneda por defecto (DOP para República Dominicana).
     * Según el ESQL se mapea como Mb.Moneda pero el servicio SOAP no lo retorna,
     * así que usamos el valor por defecto.
     * 
     * @return Código de moneda
     */
    public String getCurrency() {
        return "DOP";
    }
    
    /**
     * Obtiene el estado del movimiento.
     * Según el ESQL se mapea como Mb.Estado pero el servicio SOAP no lo retorna,
     * así que inferimos el estado basado en que tengamos datos.
     * 
     * @return Estado del movimiento
     */
    public String getStatus() {
        if (transactionNumber != null && !transactionNumber.trim().isEmpty()) {
            return "COMPLETED";
        }
        return "UNKNOWN";
    }
}