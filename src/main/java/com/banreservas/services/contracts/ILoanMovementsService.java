package com.banreservas.services.contracts;

import com.banreservas.dtos.inbound.RequestDto;
import com.banreservas.dtos.inbound.ResponseDto;
import com.fasterxml.jackson.core.JsonProcessingException;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.MultivaluedMap;

/**
 * Service interface for handling loan movements operations.
 * This interface defines the contract for retrieving the latest
 * loan movements from the backend SOAP service.
 * 
 * The service handles the business logic for:
 * - Building SOAP requests with proper date calculations
 * - Calling the backend MovimientosPrestamo service
 * - Processing and mapping the SOAP XML response
 * - Returning structured loan movement data
 * 
 * @author Integration System
 * @since 2025-07-21
 * @version 1.0
 */
public interface ILoanMovementsService {

    /**
     * Retrieves the latest loan movements for a specific product.
     * 
     * @param request The request containing product information to query
     * @param headers HTTP headers containing metadata (channel, user, date, etc.)
     * @return A Uni containing the response with loan movements data
     * @throws JsonProcessingException if there's an error processing JSON/XML data
     */
    Uni<ResponseDto> getLoanMovements(RequestDto request, MultivaluedMap<String, String> headers)
            throws JsonProcessingException;
}