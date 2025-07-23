package com.banreservas.exceptions;

import java.util.List;

import com.banreservas.dtos.inbound.ProductMovementsDto;
import com.banreservas.dtos.inbound.ProductsResponseDto;
import com.banreservas.dtos.inbound.ResponseDto;
import com.banreservas.dtos.inbound.ResponseHeaderDto;
import com.banreservas.utils.defaults.CodeMessages;

import io.quarkus.logging.Log;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Exception mapper para manejo de WebApplicationException.
 * Este mapper captura instancias de WebApplicationException y las convierte
 * a respuestas HTTP apropiadas con formato de error correcto.
 * 
 * @author Consultor Domingo Ruiz - C-DJruiz@banreservas.com
 * @since 2025-07-22
 * @version 1.0
 */
@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

    @Context
    private ContainerRequestContext requestContext;

    @Override
    public Response toResponse(WebApplicationException exception) {
        String sessionId = requestContext != null && requestContext.getHeaders() != null ? 
                          requestContext.getHeaders().getFirst("sessionId") : "unknown";
        
        int statusCode = exception.getResponse().getStatus();
        
        Log.errorf(exception, "[SessionId: %s] WebApplicationException capturada - Status: %d, Mensaje: %s", 
                  sessionId, statusCode, exception.getMessage());
        
        // Determinar mensaje de error basado en el código de estado
        String errorMessage = getErrorMessage(statusCode);
        
        // Crear header de respuesta
        ResponseHeaderDto errorHeader = new ResponseHeaderDto(statusCode, errorMessage);
        
        // Crear producto vacío para el body
        ProductMovementsDto emptyProduct = new ProductMovementsDto(
            null, null, null, List.of(), null
        );
        
        // Crear body de respuesta
        ProductsResponseDto emptyBody = new ProductsResponseDto(List.of(emptyProduct));
        
        // Crear respuesta completa con estructura estándar
        ResponseDto errorResponse = new ResponseDto(errorHeader, emptyBody);
    
        return Response.status(statusCode)
                .entity(errorResponse)
                .header("sessionId", sessionId)
                .build();
    }
    
    /**
     * Obtiene el mensaje de error apropiado basado en el código de estado HTTP.
     * 
     * @param statusCode Código de estado HTTP
     * @return Mensaje de error correspondiente
     */
    private String getErrorMessage(int statusCode) {
        return switch (statusCode) {
            case 400 -> CodeMessages.MESSAGE_BAD_REQUEST;
            case 401 -> CodeMessages.MESSAGE_UNAUTHORIZED;
            case 403 -> CodeMessages.MESSAGE_FORBIDDEN;
            case 404 -> CodeMessages.MESSAGE_NOT_FOUND;
            case 500 -> CodeMessages.MESSAGE_INTERNAL_SERVER_ERROR;
            default -> "Error en el procesamiento de la solicitud";
        };
    }
}