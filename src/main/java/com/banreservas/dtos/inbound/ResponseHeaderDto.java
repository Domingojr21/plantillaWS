package com.banreservas.dtos.inbound;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * DTO (Data Transfer Object) que representa el encabezado de una respuesta.
 * Esta clase contiene dos propiedades:
 * - {@link #responseCode}: Un código numérico que representa el código de
 * respuesta de la solicitud.
 * - {@link #responseMessage}: Un mensaje asociado con el código de respuesta.
 * 
 * La clase está registrada para reflexión a través de la anotación
 * {@link RegisterForReflection}
 * para permitir la serialización y deserialización con Quarkus.
 * 
 * Se proporcionan dos constructores:
 * - Un constructor por defecto.
 * - Un constructor que acepta tanto el código de respuesta como el mensaje.
 * 
 * @author Ing. Victor Diaz - vjdiaz@banreservas.com
 * @since 06-02-2025
 * @version 1.0
 */
@RegisterForReflection
public record ResponseHeaderDto(
        @JsonProperty("responseCode") 
        int responseCode,
        @JsonProperty("responseMessage") 
        String responseMessage) implements Serializable {
}