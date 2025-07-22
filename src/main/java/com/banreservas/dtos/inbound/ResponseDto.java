package com.banreservas.dtos.inbound;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * DTO de respuesta principal para el servicio de últimos movimientos de préstamo.
 * Contiene la lista de productos con sus respectivos movimientos consultados.
 * 
 * Este DTO representa la estructura final de respuesta que se enviará
 * al consumidor del microservicio, manteniendo la compatibilidad con
 * el formato del Integration Message de salida.
 * 
 * @author Sistema de Integración
 * @since 2025-01-28
 * @version 1.0
 */
@RegisterForReflection
public record ResponseDto(
        @JsonProperty("products") 
        List<ProductMovementsDto> products) implements Serializable {
}