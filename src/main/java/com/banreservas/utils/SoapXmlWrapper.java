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
 * XML wrapper class for deserializing SOAP XML responses.
 * This class is only used for Jackson XML deserialization and then
 * converted to the proper SoapResponseDto record.
 * 
 * This is a workaround for Jackson XML limitations with records
 * that have @JacksonXmlRootElement annotations.
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

    /**
     * Converts this XML wrapper to a proper SoapResponseDto record.
     * 
     * @return SoapResponseDto record with the movements data
     */
    public SoapResponseDto toRecord() {
        return new SoapResponseDto(movements);
    }
}