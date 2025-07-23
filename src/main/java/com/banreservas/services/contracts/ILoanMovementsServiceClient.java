package com.banreservas.services.contracts;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;

/**
 * Cliente REST para el servicio SOAP de movimientos de préstamo.
 * Maneja la comunicación HTTP con el endpoint SOAP externo.
 * 
 * @author Consultor Domingo Ruiz - C-DJruiz@banreservas.com
 * @since 2025-07-22
 * @version 1.0
 */
@RegisterRestClient(configKey = "movimientos-prestamo-service")
@ApplicationScoped
public interface ILoanMovementsServiceClient {
    
    /**
     * Realiza la petición SOAP al servicio de movimientos de préstamo.
     * 
     * @param contentType Tipo de contenido (text/xml; charset=utf-8)
     * @param requestBody XML de la solicitud SOAP
     * @return Uni<String> con la respuesta XML del servicio
     */
    @POST
    @Consumes("text/xml")
    @Produces("text/xml")
    Uni<String> getLoanMovements(@HeaderParam("Content-Type") String contentType, String requestBody);
}