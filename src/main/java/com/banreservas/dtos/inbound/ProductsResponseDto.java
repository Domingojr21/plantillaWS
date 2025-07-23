package com.banreservas.dtos.inbound;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO de respuesta principal para el servicio de últimos movimientos de préstamo.
 * Contiene la lista de productos con sus respectivos movimientos consultados.
 * @author Sistema de Integración
 * @since 2025-01-28
 * @version 1.0
 */
public record ProductsResponseDto(   
    @JsonProperty("products") 
     List<ProductMovementsDto> products) implements Serializable {
}
