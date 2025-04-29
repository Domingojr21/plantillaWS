package com.banreservas.services.contracts;

import com.banreservas.dtos.inbound.RequestDto;
import com.banreservas.dtos.inbound.ResponseValidatesJointAccountsDto;
import com.fasterxml.jackson.core.JsonProcessingException;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MultivaluedMap;

/**
 *
 * @author Ing. Victor Diaz - vjdiaz@banreservas.com
 * @since 28-01-2025
 * @version 1.0
 */

public interface IValidatesJointAccountsService {

    Uni<ResponseValidatesJointAccountsDto> validateAccounts(RequestDto request, MultivaluedMap<String, String> headers)
            throws JsonProcessingException;

}
