package com.banreservas.dtos.outbound;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * DTO record for mapping the complete XML response from the MovimientosPrestamo SOAP service.
 * This record represents the parsed loan movements data without XML annotations.
 * 
 * Since Jackson XML cannot deserialize records with @JacksonXmlRootElement,
 * we use a helper class for XML parsing and then convert to this record.
 * 
 * @author Integration System
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