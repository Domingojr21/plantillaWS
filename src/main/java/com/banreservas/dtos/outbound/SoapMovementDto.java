package com.banreservas.dtos.outbound;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * DTO record for mapping loan movements from the SOAP service XML response.
 * This DTO is used to deserialize the XML response that comes in the
 * 'XMLReresponse' field from the MovimientosPrestamo backend service.
 * 
 * The expected XML structure based on the actual SOAP response is:
 * <MovimientosPrestamo>
 *   <MovimientoPrestamo>
 *     <NumeroTransaccion>9600385144202206151523442020173</NumeroTransaccion>
 *     <Fecha>2022-06-09T00:00:00.000000</Fecha>
 *     <FechaReal>2022-06-15T00:00:00.000000</FechaReal>
 *     <TipoMovimiento>Credito</TipoMovimiento>
 *     <MontoMovimiento>8819.27</MontoMovimiento>
 *     <Concepto>Pago regular - fecha efectiva</Concepto>
 *     <Saldo>1115457.35</Saldo>
 *     <Causal>83</Causal>
 *     <IdUnico>9600385144202206151523442020173</IdUnico>
 *   </MovimientoPrestamo>
 * </MovimientosPrestamo>
 * 
 * @author Integration System
 * @since 2025-07-21
 * @version 1.0
 */
@RegisterForReflection
public record SoapMovementDto(
    @JacksonXmlProperty(localName = "NumeroTransaccion")
    @JsonProperty("numeroTransaccion")
    String transactionNumber,
    @JacksonXmlProperty(localName = "Fecha")
    @JsonProperty("fecha")
    String date,
    @JacksonXmlProperty(localName = "FechaReal")
    @JsonProperty("fechaReal")
    String realDate,
    @JacksonXmlProperty(localName = "TipoMovimiento")
    @JsonProperty("tipoMovimiento")
    String movementType,
    @JacksonXmlProperty(localName = "MontoMovimiento")
    @JsonProperty("montoMovimiento")
    BigDecimal amount,
    @JacksonXmlProperty(localName = "Concepto")
    @JsonProperty("concepto")
    String description,
    @JacksonXmlProperty(localName = "Saldo")
    @JsonProperty("saldo")
    BigDecimal balance,
    @JacksonXmlProperty(localName = "Causal")
    @JsonProperty("causal")
    String causal,
    @JacksonXmlProperty(localName = "IdUnico")
    @JsonProperty("idUnico")
    String uniqueId

) implements Serializable {

    public String getCurrency() {
        return "DOP";
    }

    public String getStatus() {
        return movementType;
    }
}
