package com.banreservas.resources;

import java.util.List;

import org.jboss.logging.Logger;

import com.banreservas.dtos.inbound.ProductMovementsDto;
import com.banreservas.dtos.inbound.ProductsResponseDto;
import com.banreservas.dtos.inbound.RequestDto;
import com.banreservas.dtos.inbound.ResponseDto;
import com.banreservas.dtos.inbound.ResponseHeaderDto;
import com.banreservas.services.contracts.ILoanMovementsService;
import com.banreservas.utils.BuildJsonConstructLogAppender;
import com.banreservas.utils.defaults.CodeMessages;
import com.banreservas.utils.defaults.RequestHeaders;
import com.banreservas.utils.defaults.RequestHeadersValidator;
import com.fasterxml.jackson.core.JsonProcessingException;

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
 * Recurso REST para consulta de movimientos de préstamo.
 * Proporciona endpoints para recuperar movimientos de préstamo
 * desde el servicio SOAP backend.
 * 
 * @author Consultor Domingo Ruiz - C-DJruiz@banreservas.com
 * @since 2025-07-22
 * @version 1.0
 */
@Path("/v1")
//@Authenticated
@ApplicationScoped
public class LoanMovementsResource {

    private static final Logger LOG = Logger.getLogger(LoanMovementsResource.class);

    @Inject
    private ILoanMovementsService loanMovementsService;

    /**
     * Endpoint para recuperar movimientos de préstamo de un producto específico.
     * 
     * @param requestDto  DTO con la información de la solicitud
     * @param httpHeaders Encabezados de la solicitud HTTP
     * @return Respuesta con los movimientos de préstamo
     * @throws JsonProcessingException Si ocurre un error al procesar el JSON
     */
    @POST
    @Path("/ultimos-movimientos-prestamo")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Response> getLoanMovements(@Valid RequestDto requestDto, @Context HttpHeaders httpHeaders)
            throws JsonProcessingException {

        String sessionId = httpHeaders.getRequestHeaders().getFirst("sessionId");
        
        LOG.infof("Iniciando consulta de movimientos para producto: %s", requestDto.productNumber());

        String headerValidationResult = RequestHeadersValidator.validateRequestHeaders(httpHeaders);
        if (!RequestHeadersValidator.VALID.equalsIgnoreCase(headerValidationResult)) {
            LOG.warnf("Validación de headers falló: %s", headerValidationResult);
            
            ResponseHeaderDto errorHeader = new ResponseHeaderDto(400, headerValidationResult);
        ProductMovementsDto emptyProduct = new ProductMovementsDto(null, null, null, List.of(), null);
        ProductsResponseDto emptyBody = new ProductsResponseDto(List.of(emptyProduct));
        ResponseDto errorResponse = new ResponseDto(errorHeader, emptyBody);
        
        return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                .entity(errorResponse)
                .header("sessionId", sessionId)
                .build());
        }

        MultivaluedMap<String, String> headers = httpHeaders.getRequestHeaders();

        return loanMovementsService.getLoanMovements(requestDto, headers)
                .onItem().transform(productsResponseDto -> {
                    // Validar si realmente hay datos exitosos
                    if (productsResponseDto != null && 
                        productsResponseDto.products() != null && 
                        !productsResponseDto.products().isEmpty()) {
                        
                        ProductMovementsDto product = productsResponseDto.products().get(0);
                        
                        if (product.movements() != null && !product.movements().isEmpty()) {
                            // Hay movimientos - respuesta exitosa
                            LOG.infof("Consulta completada exitosamente para producto: %s con %d movimientos", 
                                     requestDto.productNumber(), product.movements().size());
                            
                            ResponseHeaderDto successHeader = new ResponseHeaderDto(200, CodeMessages.MESSAGE_SUCCESS);
                            ResponseDto successResponse = new ResponseDto(successHeader, productsResponseDto);
                            
                            Response.ResponseBuilder responseBuilder = Response.ok(successResponse);

                            for (String headerName : RequestHeaders.HEADERS) {
                                String headerValue = headers.getFirst(headerName);
                                if (headerValue != null) {
                                    responseBuilder.header(headerName, headerValue);
                                }
                            }

                            return responseBuilder.build();
                        } else {
                            // Producto existe pero no tiene movimientos - respuesta 200 con datos vacíos
                            LOG.infof("Producto %s existe pero no tiene movimientos en el rango consultado", 
                                     requestDto.productNumber());
                            
                            ResponseHeaderDto successHeader = new ResponseHeaderDto(200, "No se encontraron movimientos para el producto en el rango de fechas consultado");
                            ResponseDto successResponse = new ResponseDto(successHeader, productsResponseDto);
                            
                            Response.ResponseBuilder responseBuilder = Response.ok(successResponse);

                            for (String headerName : RequestHeaders.HEADERS) {
                                String headerValue = headers.getFirst(headerName);
                                if (headerValue != null) {
                                    responseBuilder.header(headerName, headerValue);
                                }
                            }

                            return responseBuilder.build();
                        }
                    } else {
                        // Caso extremo - no hay estructura de datos
                        LOG.warnf("Respuesta sin estructura de datos para producto: %s", requestDto.productNumber());
                        
                        ResponseHeaderDto notFoundHeader = new ResponseHeaderDto(404, CodeMessages.MESSAGE_NOT_FOUND);
                        ProductMovementsDto emptyProduct = new ProductMovementsDto(
                            requestDto.productNumber(), requestDto.productLine(), requestDto.currency(), List.of(), null);
                        ProductsResponseDto emptyBody = new ProductsResponseDto(List.of(emptyProduct));
                        ResponseDto notFoundResponse = new ResponseDto(notFoundHeader, emptyBody);
                        
                        return Response.status(Response.Status.NOT_FOUND)
                                .entity(notFoundResponse)
                                .header("sessionId", sessionId)
                                .build();
                    }
                })
                .onFailure(throwable -> !(throwable instanceof jakarta.ws.rs.NotFoundException))
                .recoverWithItem(throwable -> {
                    LOG.errorf("Error en consulta de movimientos: %s", throwable.getMessage());
                    
                    // Solo manejar casos específicos que no tienen exception mapper propio
                    // NotFoundException se maneja automáticamente por NotFoundExceptionMapper
                    int statusCode = 500;
                    String errorMessage = CodeMessages.MESSAGE_INTERNAL_SERVER_ERROR;
                    
                    if (throwable instanceof IllegalArgumentException) {
                        statusCode = 400;
                        errorMessage = throwable.getMessage();
                    } else if (throwable.getMessage() != null && 
                              (throwable.getMessage().toLowerCase().contains("timeout") || 
                               throwable.getMessage().toLowerCase().contains("connection"))) {
                        statusCode = 503;
                        errorMessage = "Servicio no disponible temporalmente";
                    }
                    
                    ResponseHeaderDto errorHeader = new ResponseHeaderDto(statusCode, errorMessage);
                    ProductMovementsDto emptyProduct = new ProductMovementsDto(
                        requestDto.productNumber(), requestDto.productLine(), requestDto.currency(), List.of(), null);
                    ProductsResponseDto emptyBody = new ProductsResponseDto(List.of(emptyProduct));
                    ResponseDto errorResponse = new ResponseDto(errorHeader, emptyBody);
                    
                    return Response.status(statusCode)
                            .entity(errorResponse)
                            .header("sessionId", sessionId)
                            .build();
                });
    }
}