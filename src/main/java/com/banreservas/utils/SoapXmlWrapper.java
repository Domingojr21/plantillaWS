package com.banreservas.utils;

import java.io.Serializable;
import java.util.List;

import com.banreservas.dtos.outbound.SoapMovementDto;
import com.banreservas.dtos.outbound.SoapResponseDto;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Wrapper XML simplificado para deserializar respuestas SOAP.
 * Se usa únicamente para mapeo XML de Jackson.
 * 
 * @author Consultor Domingo Ruiz - C-DJruiz@banreservas.com
 * @since 2025-07-22
 * @version 1.0
 */
@RegisterForReflection
@JacksonXmlRootElement(localName = "MovimientosPrestamo")
public class SoapXmlWrapper implements Serializable {

    @JacksonXmlProperty(localName = "MovimientoPrestamo")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<SoapMovementDto> movements;

    public SoapXmlWrapper() {
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