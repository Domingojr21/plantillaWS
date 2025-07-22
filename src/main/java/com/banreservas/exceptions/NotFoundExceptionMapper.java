package com.banreservas.exceptions;

import java.util.List;

import org.jboss.logging.Logger;

import com.banreservas.dtos.inbound.ProductMovementsDto;
import com.banreservas.dtos.inbound.ResponseDto;
import com.banreservas.dtos.inbound.ResponseHeaderDto;
import com.banreservas.utils.defaults.ErrorResponse;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Exception mapper for handling NotFoundException.
 * This mapper catches NotFoundException instances and converts them
 * to appropriate HTTP responses with proper error formatting.
 * 
 * @author Integration System
 * @since 2025-07-21
 * @version 1.0
 */
@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

    @Override
    public Response toResponse(NotFoundException exception) {
        ProductMovementsDto emptyProduct = new ProductMovementsDto(
            null, null, null, List.of(), null
        );
        
        ResponseDto errorResponse = new ResponseDto(List.of(emptyProduct));
        
        return Response.status(Response.Status.NOT_FOUND)
                .entity(errorResponse)
                .build();
    }
}