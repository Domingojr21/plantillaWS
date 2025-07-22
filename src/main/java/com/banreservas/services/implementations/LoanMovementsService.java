package com.banreservas.services.implementations;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Retry;

import com.banreservas.dtos.inbound.MovementDto;
import com.banreservas.dtos.inbound.PaginationDto;
import com.banreservas.dtos.inbound.ProductMovementsDto;
import com.banreservas.dtos.inbound.RequestDto;
import com.banreservas.dtos.inbound.ResponseDto;
import com.banreservas.dtos.outbound.SoapMovementDto;
import com.banreservas.dtos.outbound.SoapResponseDto;
import com.banreservas.utils.SoapXmlWrapper;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import com.banreservas.services.contracts.IDateUtilService;
import com.banreservas.services.contracts.ILoanMovementsService;
import com.brrd.service.services.ImovimientosPrestamoRequest;
import com.brrd.service.services.MovimientosPrestamoRequest;
import com.brrd.service.services.ServiceResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import io.quarkiverse.cxf.annotation.CXFClient;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedMap;

/**
 * Implementation of the loan movements service that handles the business logic
 * for retrieving loan movements from the backend SOAP service.
 * 
 * This service replicates the exact logic from the ESQL transformation
 * UltimosMovimientosPrestamoCRM_Request and UltimosMovimientosPrestamoCRM_Response,
 * including date calculations and XML response processing.
 * 
 * @author Integration System
 * @since 2025-07-21
 * @version 1.0
 */
@ApplicationScoped
public class LoanMovementsService implements ILoanMovementsService {

    @Inject
    @CXFClient("ultimosMovimientosPrestamo")
    private ImovimientosPrestamoRequest movimientosPrestamoClient;

    @Inject
    private IDateUtilService dateUtilService;

    @ConfigProperty(name = "cantidad.movimientos")
    private int cantidadMovimientos;

    @ConfigProperty(name = "direccion.consulta")
    private String direccionConsulta;

    @ConfigProperty(name = "monto.inicial")
    private long montoInicial;

    @ConfigProperty(name = "monto.final")
    private long montoFinal;

    @ConfigProperty(name = "record.inicial")
    private int recordInicial;

    @ConfigProperty(name = "tipo.transaccion")
    private String tipoTransaccion;

    @ConfigProperty(name = "meses.atras")
    private int mesesAtras;

    /**
     * {@inheritDoc}
     * 
     * This method replicates the logic from UltimosMovimientosPrestamoCRM_Request and 
     * UltimosMovimientosPrestamoCRM_Response ESQL procedures.
     */
    @Override
    @Retry(maxRetries = 3, delay = 500, retryOn = { RuntimeException.class })
    @CircuitBreaker(requestVolumeThreshold = 4, failureRatio = 0.5, delay = 1000)
    public Uni<ResponseDto> getLoanMovements(RequestDto request, MultivaluedMap<String, String> headers)
            throws JsonProcessingException {

        MovimientosPrestamoRequest soapRequest = buildSoapRequest(request, headers);

        Log.infov("SOAP Request for product: {0}", request.productNumber());

        return Uni.createFrom().item(() -> {
            try {
                ServiceResponse soapResponse = movimientosPrestamoClient.movimientosPrestamo(soapRequest);

                if ("000".equals(soapResponse.getErrorCode()) && "SUCCESS".equals(soapResponse.getErrorType())) {
                    return processSuccessfulResponse(soapResponse, request);
                } else {
                    Log.errorv("SOAP service error - Code: {0}, Message: {1}", 
                              soapResponse.getErrorCode(), soapResponse.getErrorMessage());
                    throw new IllegalArgumentException(soapResponse.getErrorMessage());
                }

            } catch (Exception e) {
                Log.errorv("Error calling SOAP service: {0}", e.getMessage());
                throw new RuntimeException(e.getMessage());
            }
        }).runSubscriptionOn(Infrastructure.getDefaultExecutor());
    }

    /**
     * Builds the SOAP request replicating the exact mapping from 
     * UltimosMovimientosPrestamoCRM_Request ESQL procedure.
     * 
     */
    private MovimientosPrestamoRequest buildSoapRequest(RequestDto request, MultivaluedMap<String, String> headers) {
        MovimientosPrestamoRequest soapRequest = new MovimientosPrestamoRequest();

        soapRequest.setChannel(headers.getFirst("id_consumidor"));
        soapRequest.setDate(headers.getFirst("fecha_hora"));
        soapRequest.setOperationName("movimientosPrestamo");
        soapRequest.setTerminal(headers.getFirst("terminal"));
        soapRequest.setUser(headers.getFirst("usuario"));

        soapRequest.setCantidad(cantidadMovimientos);
        soapRequest.setDireccion(direccionConsulta);
        
        // Convert String dates to XMLGregorianCalendar
        try {
            String fechaFinalStr = dateUtilService.getCurrentDateFormatted();
            String fechaInicialStr = dateUtilService.calculatePreviousDateByMonths(mesesAtras);
            
            soapRequest.setFechaFinal(convertToXMLGregorianCalendar(fechaFinalStr));
            soapRequest.setFechaInicial(convertToXMLGregorianCalendar(fechaInicialStr));
        } catch (Exception e) {
            throw new RuntimeException("Error converting dates to XMLGregorianCalendar", e);
        }
        
        // Convert long to Double for amounts
        soapRequest.setMontoFinal(Double.valueOf(montoFinal));
        soapRequest.setMontoInicial(Double.valueOf(montoInicial));
        
        soapRequest.setProducto(request.productNumber());
        soapRequest.setRecord(recordInicial);
        soapRequest.setTipo(tipoTransaccion);

        return soapRequest;
    }

    /**
     * Converts a date string to XMLGregorianCalendar.
     * Assumes the input format is yyyy-MM-dd or similar.
     */
    private XMLGregorianCalendar convertToXMLGregorianCalendar(String dateStr) throws Exception {
        // Parse the date string (adjust format as needed based on your DateUtilService output)
        LocalDate localDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        // Convert to XMLGregorianCalendar
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(
            localDate.getYear(),
            localDate.getMonthValue(),
            localDate.getDayOfMonth(),
            0, 0, 0, 0, 0
        );
    }

    /**
     * Processes a successful SOAP response replicating the logic from
     * UltimosMovimientosPrestamoCRM_Response ESQL procedure.
     * 
     */
    private ResponseDto processSuccessfulResponse(ServiceResponse soapResponse, RequestDto request) 
            throws JsonProcessingException {
        
        String xmlContent = soapResponse.getXMLReresponse();
        
        if (xmlContent == null || xmlContent.trim().isEmpty()) {
            Log.warn("Empty XML response from SOAP service");
            return createEmptyResponse(request);
        }

        try {
            XmlMapper xmlMapper = new XmlMapper();
            SoapXmlWrapper xmlWrapper = xmlMapper.readValue(xmlContent, SoapXmlWrapper.class);
            SoapResponseDto parsedResponse = xmlWrapper.toRecord();

            List<MovementDto> movements = mapSoapMovementsToDto(parsedResponse.movements());
            
            PaginationDto pagination = null;
            if (movements != null && !movements.isEmpty()) {
                String lastUniqueId = movements.get(movements.size() - 1).uniqueId();
                pagination = new PaginationDto(lastUniqueId);
            }

            ProductMovementsDto productMovements = new ProductMovementsDto(
                request.productNumber(),
                request.productLine(), 
                request.currency(),
                movements,
                pagination
            );

            Log.infov("Successfully processed {0} movements for product {1}", 
                     movements != null ? movements.size() : 0, request.productNumber());

            return new ResponseDto(List.of(productMovements));

        } catch (Exception e) {
            Log.errorv("Error parsing XML response: {0}", e.getMessage());
            throw new RuntimeException("Error processing SOAP response", e);
        }
    }

    /**
     * Maps SOAP movement DTOs to response movement DTOs.
     * This replicates the SELECT mapping from the ESQL response procedure.
     */
    private List<MovementDto> mapSoapMovementsToDto(List<SoapMovementDto> soapMovements) {
        if (soapMovements == null) {
            return List.of();
        }

        return soapMovements.stream()
            .map(soapMovement -> new MovementDto(
                soapMovement.getCurrency(),  // Helper method returns "DOP" 
                soapMovement.amount(),
                soapMovement.date(),
                soapMovement.description(),
                soapMovement.getStatus(), // Helper method returns movementType
                soapMovement.transactionNumber(),
                soapMovement.uniqueId(),
                soapMovement.causal()
            ))
            .collect(Collectors.toList());
    }

    /**
     * Creates an empty response when no movements are found.
     */
    private ResponseDto createEmptyResponse(RequestDto request) {
        ProductMovementsDto productMovements = new ProductMovementsDto(
            request.productNumber(),
            request.productLine(),
            request.currency(),
            List.of(),
            null
        );
        
        return new ResponseDto(List.of(productMovements));
    }
}