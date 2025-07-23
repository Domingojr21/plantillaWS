package com.banreservas.dtos.outbound;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * DTO para mapear la respuesta XML completa del servicio SOAP MovimientosPrestamo.
 * 
 * @author Consultor Domingo Ruiz - C-DJruiz@banreservas.com
 * @since 2025-07-21
 * @version 1.0
 */
@RegisterForReflection
public record SoapResponseDto(
    @JsonProperty("movimientosPrestamo")
    List<SoapMovementDto> movements
) implements Serializable {

    public List<SoapMovementDto> getMovements() {
        return movements;
    }
}