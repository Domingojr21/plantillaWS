package com.banreservas.services.contracts;

import com.banreservas.dtos.inbound.ProductsResponseDto;
import com.banreservas.dtos.inbound.RequestDto;
import com.banreservas.dtos.inbound.ResponseDto;
import com.fasterxml.jackson.core.JsonProcessingException;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.MultivaluedMap;

/**
 * Servicio para manejo de operaciones de movimientos de préstamo.
 * Define el contrato para recuperar los últimos movimientos 
 * de préstamo desde el servicio SOAP backend.
 * 
 * @author Integration System
 * @since 2025-07-21
 * @version 1.0
 */
public interface ILoanMovementsService {

     /**
     * Recupera los últimos movimientos de préstamo para un producto específico.
     * 
     * @param request La solicitud conteniendo información del producto a consultar
     * @param headers Encabezados HTTP conteniendo metadatos (canal, usuario, fecha, etc.)
     * @return Un Uni conteniendo la respuesta con datos de movimientos de préstamo
     * @throws JsonProcessingException si hay error procesando datos JSON/XML
     */
    Uni<ProductsResponseDto> getLoanMovements(RequestDto request, MultivaluedMap<String, String> headers)
            throws JsonProcessingException;
}