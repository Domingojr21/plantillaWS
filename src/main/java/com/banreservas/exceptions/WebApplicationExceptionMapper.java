package com.banreservas.exceptions;

import java.util.List;

import com.banreservas.dtos.inbound.ProductMovementsDto;
import com.banreservas.dtos.inbound.ResponseDto;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Exception mapper for handling WebApplicationException.
 * This mapper catches WebApplicationException instances and converts them
 * to appropriate HTTP responses with proper error formatting.
 * 
 * @author Integration System
 * @since 2025-07-21
 * @version 1.0
 */
@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

    @Override
    public Response toResponse(WebApplicationException exception) {
        ProductMovementsDto emptyProduct = new ProductMovementsDto(
            null, null, null, List.of(), null
        );
        
        ResponseDto errorResponse = new ResponseDto(List.of(emptyProduct));
        
        return Response.status(exception.getResponse().getStatus())
                .entity(errorResponse)
                .build();
    }
}