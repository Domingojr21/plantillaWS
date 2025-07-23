package com.banreservas.resources;

import java.util.List;

import com.banreservas.dtos.inbound.ProductMovementsDto;
import com.banreservas.dtos.inbound.ProductsResponseDto;
import com.banreservas.dtos.inbound.RequestDto;
import com.banreservas.dtos.inbound.ResponseDto;
import com.banreservas.dtos.inbound.ResponseHeaderDto;
import com.banreservas.services.contracts.ILoanMovementsService;
import com.banreservas.utils.defaults.RequestHeadersValidator;
import com.banreservas.utils.defaults.CodeMessages;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.banreservas.utils.BuildJsonConstructLogAppender;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

/**
 * Recurso REST para manejo de operaciones de movimientos de préstamo.
 * Proporciona endpoints para recuperar movimientos de préstamo
 * desde los sistemas backend mediante integración SOAP.
 * 
 * @author Integration System
 * @since 2025-07-21
 * @version 1.0
 */
@Path("/v1")
//@Authenticated
@ApplicationScoped
public class LoanMovementsResource {

    @Inject
    private ILoanMovementsService loanMovementsService;

    /**
     * Endpoint para recuperar movimientos de préstamo de un producto específico.
     * 
     * @param requestDto La solicitud conteniendo información del producto
     * @param httpHeaders Encabezados HTTP conteniendo metadatos
     * @return Response conteniendo datos de movimientos de préstamo
     * @throws JsonProcessingException si hay error procesando datos
     */
    @POST
    //@RolesAllowed("ultimos-movimientos-prestamo-crm")
    @Path("/ultimos-movimientos-prestamo")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
   public Uni<Response> getLoanMovements(@Valid RequestDto requestDto, @Context HttpHeaders httpHeaders)
        throws JsonProcessingException {

    String sessionId = httpHeaders.getRequestHeaders().getFirst("sessionId");
    
    Log.infof("[SessionId: %s] Iniciando consulta de movimientos para producto: %s", 
             sessionId, requestDto.productNumber());
    
    Log.debugf("[SessionId: %s] Payload del request: %s", 
              sessionId, BuildJsonConstructLogAppender.buildJson(requestDto));

    MultivaluedMap<String, String> headers = httpHeaders.getRequestHeaders();
    
    // Log detallado de TODOS los headers recibidos
    Log.infof("[SessionId: %s] === DEBUGGING HEADERS RECIBIDOS ===", sessionId);
    Log.infof("[SessionId: %s] Total de headers: %d", sessionId, headers.size());
    
    headers.forEach((key, values) -> {
        Log.infof("[SessionId: %s] Header '%s' = %s", sessionId, key, values);
    });
    
    // Log específico de los headers requeridos (en inglés)
    Log.infof("[SessionId: %s] === HEADERS REQUERIDOS ===", sessionId);
    Log.infof("[SessionId: %s] sessionId: '%s'", sessionId, headers.getFirst("sessionId"));
    Log.infof("[SessionId: %s] channel: '%s'", sessionId, headers.getFirst("channel"));
    Log.infof("[SessionId: %s] user: '%s'", sessionId, headers.getFirst("user"));
    Log.infof("[SessionId: %s] dateTime: '%s'", sessionId, headers.getFirst("dateTime"));
    Log.infof("[SessionId: %s] terminal: '%s'", sessionId, headers.getFirst("terminal"));
    Log.infof("[SessionId: %s] operation: '%s'", sessionId, headers.getFirst("operation"));

    String headerValidationResult = RequestHeadersValidator.validateRequestHeaders(httpHeaders);
    if (!RequestHeadersValidator.VALID.equalsIgnoreCase(headerValidationResult)) {
        Log.warnf("[SessionId: %s] Validación de headers falló: %s", sessionId, headerValidationResult);
        
        // Crear header de error
        ResponseHeaderDto errorHeader = new ResponseHeaderDto(400, headerValidationResult);
        
        // Crear body vacío
        ProductMovementsDto emptyProduct = new ProductMovementsDto(null, null, null, List.of(), null);
        ProductsResponseDto emptyBody = new ProductsResponseDto(List.of(emptyProduct));
        
        // Crear respuesta completa
        ResponseDto errorResponse = new ResponseDto(errorHeader, emptyBody);
        
        Log.debugf("[SessionId: %s] Retornando respuesta 400 por validación de headers: %s", 
                  sessionId, BuildJsonConstructLogAppender.buildJson(errorResponse));
        
        return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                .entity(errorResponse)
                .header("sessionId", sessionId)
                .build());
    }

    Log.infof("[SessionId: %s] Headers validados correctamente, delegando al servicio", sessionId);

    return loanMovementsService.getLoanMovements(requestDto, headers)
            .onItem().transform(productsResponseDto -> {
                Log.infof("[SessionId: %s] Servicio completado exitosamente", sessionId);
                Log.debugf("[SessionId: %s] Respuesta del servicio: %s", 
                          sessionId, BuildJsonConstructLogAppender.buildJson(productsResponseDto));
                
                // Crear header de éxito
                ResponseHeaderDto successHeader = new ResponseHeaderDto(200, CodeMessages.MESSAGE_SUCCESS);
                
                // Crear respuesta completa
                ResponseDto successResponse = new ResponseDto(successHeader, productsResponseDto);
                
                return Response.ok(successResponse)
                        .header("channel", headers.getFirst("channel"))
                        .header("user", headers.getFirst("user"))
                        .header("dateTime", headers.getFirst("dateTime"))
                        .header("terminal", headers.getFirst("terminal"))
                        .header("operation", headers.getFirst("operation"))
                        .header("sessionId", sessionId)
                        .build();
            })
            .onFailure().recoverWithItem(throwable -> {
                Log.errorf(throwable, "[SessionId: %s] Servicio falló con error: %s", 
                          sessionId, throwable.getMessage());
                
                // Crear header de error
                ResponseHeaderDto errorHeader = new ResponseHeaderDto(500, CodeMessages.MESSAGE_INTERNAL_SERVER_ERROR);
                
                // Crear body vacío
                ProductMovementsDto emptyProduct = new ProductMovementsDto(null, null, null, List.of(), null);
                ProductsResponseDto emptyBody = new ProductsResponseDto(List.of(emptyProduct));
                
                // Crear respuesta completa
                ResponseDto errorResponse = new ResponseDto(errorHeader, emptyBody);
                
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(errorResponse)
                        .header("sessionId", sessionId)
                        .build();
            });
}
}