package com.banreservas.exceptions;

import java.util.List;

import org.jboss.logging.Logger;

import com.banreservas.dtos.inbound.ProductMovementsDto;
import com.banreservas.dtos.inbound.ProductsResponseDto;
import com.banreservas.dtos.inbound.ResponseDto;
import com.banreservas.dtos.inbound.ResponseHeaderDto;
import com.banreservas.utils.defaults.CodeMessages;
import com.banreservas.utils.defaults.ErrorResponse;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
/**
 * Exception mapper para manejo de NotFoundException.
 * Este mapper captura instancias de NotFoundException y las convierte
 * a respuestas HTTP apropiadas con formato de error correcto.
 * 
 * @author Sistema de Integración
 * @since 2025-07-22
 * @version 1.0
 */
@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

    @Context
    private ContainerRequestContext requestContext;

    private static final Logger LOG = Logger.getLogger(NotFoundExceptionMapper.class);

    @Override
    public Response toResponse(NotFoundException exception) {
        String sessionId = requestContext != null && requestContext.getHeaders() != null ? 
                          requestContext.getHeaders().getFirst("sessionId") : "unknown";
        
        LOG.warnf("[SessionId: %s] NotFoundException capturada: %s", sessionId, exception.getMessage());
        
        ResponseHeaderDto errorHeader = new ResponseHeaderDto(404, CodeMessages.MESSAGE_NOT_FOUND);
        
        ProductMovementsDto emptyProduct = new ProductMovementsDto(
            null, null, null, List.of(), null
        );
        
        ProductsResponseDto emptyBody = new ProductsResponseDto(List.of(emptyProduct));
        
        ResponseDto errorResponse = new ResponseDto(errorHeader, emptyBody);
        
        LOG.debugf("[SessionId: %s] Retornando respuesta 404", sessionId);
        
        return Response.status(Response.Status.NOT_FOUND)
                .entity(errorResponse)
                .header("sessionId", sessionId)
                .build();
    }
}