package com.banreservas.dtos.outbound;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class ValidProductWsDto {

    public ValidProductWsDto(String productIsValid) {
        this.productIsValid = productIsValid;
    }

    public ValidProductWsDto() {
    }

    @JacksonXmlProperty(localName = "Resultado")
    private String productIsValid;

    public String getProductIsValid() {
        return productIsValid;
    }

    public void setProductIsValid(String productIsValid) {
        this.productIsValid = productIsValid;
    }
}
