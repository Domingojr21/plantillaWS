package com.banreservas.resources;

import java.util.List;

import com.banreservas.dtos.inbound.ProductMovementsDto;
import com.banreservas.dtos.inbound.RequestDto;
import com.banreservas.dtos.inbound.ResponseDto;
import com.banreservas.services.contracts.ILoanMovementsService;
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
 * REST resource for handling loan movements operations.
 * This resource provides endpoints for retrieving loan movements
 * from the backend systems through SOAP integration.
 * 
 * The resource follows the principle of having no business logic,
 * it only handles:
 * - Request reception
 * - Header extraction and validation
 * - Service delegation
 * - Response formatting
 * - Required header forwarding
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
     * Endpoint to retrieve loan movements for a specific product.
     * 
     * This endpoint:
     * 1. Validates required HTTP headers
     * 2. Delegates to the service layer for business logic
     * 3. Returns the response with appropriate headers
     * 
     * @param requestDto The request containing product information
     * @param httpHeaders HTTP headers containing metadata
     * @return Response containing loan movements data
     * @throws JsonProcessingException if there's an error processing data
     */
    @POST
    //@RolesAllowed("ultimos-movimientos-prestamo-crm")
    @Path("/ultimos-movimientos-prestamo")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Response> getLoanMovements(@Valid RequestDto requestDto, @Context HttpHeaders httpHeaders)
            throws JsonProcessingException {

        String headerValidationResult = RequestHeadersValidator.validateRequestHeaders(httpHeaders);
        if (!RequestHeadersValidator.VALID.equalsIgnoreCase(headerValidationResult)) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseDto(
                            List.of(new ProductMovementsDto(
                                    null, null, null, List.of(), null))))
                    .header("sessionId", httpHeaders.getRequestHeaders().getFirst("sessionId"))
                    .build());
        }

        MultivaluedMap<String, String> headers = httpHeaders.getRequestHeaders();

        return loanMovementsService.getLoanMovements(requestDto, headers)
                .onItem().transform(responseDto -> {
                    return Response.ok(responseDto)
                            .header("id_consumidor", headers.getFirst("id_consumidor"))
                            .header("usuario", headers.getFirst("usuario"))
                            .header("fecha_hora", headers.getFirst("fecha_hora"))
                            .header("terminal", headers.getFirst("terminal"))
                            .header("operacion", headers.getFirst("operacion"))
                            .header("sessionId", headers.getFirst("sessionId"))
                            .build();
                });
    }
}