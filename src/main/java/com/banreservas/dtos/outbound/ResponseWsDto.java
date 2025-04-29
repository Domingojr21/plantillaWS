package com.banreservas.dtos.outbound;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class ResponseWsDto {

    public ResponseWsDto() {
    }

    public ResponseWsDto(List<ValidProductWsDto> validProduct) {
        this.validProduct = validProduct;
    }

    @JacksonXmlProperty(localName = "ProductoValido")
    @JacksonXmlElementWrapper(useWrapping = false)
    List<ValidProductWsDto> validProduct;

    public List<ValidProductWsDto> getValidProduct() {
        return validProduct;
    }

    public void setValidProduct(List<ValidProductWsDto> validProduct) {
        this.validProduct = validProduct;
    }

}
