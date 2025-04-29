package com.banreservas.dtos.inbound;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 *
 * @author Ing. Victor Diaz - vjdiaz@banreservas.com
 * @since 28-01-2025
 * @version 1.0
 */

@RegisterForReflection
public record ResponseValidatesJointAccountsDto(
        @JsonProperty("isValidProduct") boolean isValidProduct) implements Serializable {
}
