package com.banreservas.utils;

import java.io.Serializable;
import java.util.List;

import com.banreservas.dtos.outbound.SoapMovementDto;
import com.banreservas.dtos.outbound.SoapResponseDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Clase wrapper XML para deserializar respuestas XML de SOAP.
 * Esta clase se usa únicamente para deserialización XML de Jackson
 * y luego se convierte al record SoapResponseDto apropiado.
 * 
 * @author Integration System
 * @since 2025-07-21
 * @version 1.0
 */
@RegisterForReflection
@JacksonXmlRootElement(localName = "MovimientosPrestamo")
public class SoapXmlWrapper implements Serializable {

    @JacksonXmlProperty(localName = "MovimientoPrestamo")
    @JacksonXmlElementWrapper(useWrapping = false)
    @JsonProperty("movimientosPrestamo")
    private List<SoapMovementDto> movements;

    public SoapXmlWrapper() {
    }

    public SoapXmlWrapper(List<SoapMovementDto> movements) {
        this.movements = movements;
    }

    public List<SoapMovementDto> getMovements() {
        return movements;
    }

    public void setMovements(List<SoapMovementDto> movements) {
        this.movements = movements;
    }

    public SoapResponseDto toRecord() {
        return new SoapResponseDto(movements);
    }
}