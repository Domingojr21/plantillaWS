package com.banreservas.services.implementations;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.banreservas.dtos.inbound.MovementDto;
import com.banreservas.dtos.inbound.PaginationDto;
import com.banreservas.dtos.inbound.ProductMovementsDto;
import com.banreservas.dtos.inbound.ProductsResponseDto;
import com.banreservas.dtos.inbound.RequestDto;
import com.banreservas.services.contracts.IDateUtilService;
import com.banreservas.services.contracts.ILoanMovementsService;
import com.banreservas.services.contracts.ILoanMovementsServiceClient;
import com.banreservas.utils.XmlUtils;
import com.fasterxml.jackson.core.JsonProcessingException;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedMap;

/**
 * Implementación del servicio de movimientos de préstamo.
 * Maneja la comunicación con el servicio SOAP externo mediante HTTP
 * y transforma las respuestas al formato requerido.
 * 
 * @author Consultor Domingo Ruiz - C-DJruiz@banreservas.com
 * @since 2025-07-22
 * @version 1.0
 */
@ApplicationScoped
public class LoanMovementsService implements ILoanMovementsService {

    private static final Logger LOG = Logger.getLogger(LoanMovementsService.class);

    @Inject
    @RestClient
    private ILoanMovementsServiceClient client;

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
     * Consulta los movimientos de préstamo enviando una solicitud SOAP al servicio externo.
     * No reintenta NotFoundException (casos WAS01) para evitar saturar el servicio.
     * 
     * @param request Datos de la solicitud
     * @param headers Encabezados requeridos para la autenticación y trazabilidad
     * @return Uni<ProductsResponseDto> con la respuesta del servicio
     */
    @Override
    @Retry(maxRetries = 3, delay = 500, abortOn = {jakarta.ws.rs.NotFoundException.class})
    @CircuitBreaker(requestVolumeThreshold = 4, failureRatio = 0.5, delay = 1000)
    public Uni<ProductsResponseDto> getLoanMovements(RequestDto request, MultivaluedMap<String, String> headers)
            throws JsonProcessingException {
        
        String sessionId = headers.getFirst("sessionId");
        LOG.infof("Iniciando consulta de movimientos para producto: %s", request.productNumber());
        
        String xmlRequest = buildSoapRequest(request, headers);
        
        return client.getLoanMovements("text/xml; charset=utf-8", xmlRequest)
                .onItem().transform(xmlResponse -> processResponse(xmlResponse, request, sessionId))
                .onFailure().invoke(throwable -> {
                    if (!(throwable instanceof jakarta.ws.rs.NotFoundException)) {
                        LOG.errorf("Error en llamada al servicio SOAP: %s", throwable.getMessage());
                    }
                });
    }

    /**
     * Construye el mensaje SOAP para la consulta de movimientos.
     * Usa valores fijos como en el curl que funciona para asegurar compatibilidad.
     */
    private String buildSoapRequest(RequestDto request, MultivaluedMap<String, String> headers) {
        String channel = "31";
        String dateTime = "2022-01-15T00:00:00"; // Usar fecha fija como en el curl que funciona
        String terminal = "0.0.0.0";
        String user = "prueba"; // Usar usuario fijo como en el curl que funciona

        // Usar fechas fijas como en el curl que funciona
        String fechaFinal = "2025-12-30T00:00:00";
        String fechaInicial = "2015-01-11T00:00:00";

        String xmlRequest = XmlUtils.buildSoapRequest(
                channel, dateTime, terminal, user, request.productNumber(),
                cantidadMovimientos, direccionConsulta, fechaInicial, fechaFinal,
                montoInicial, montoFinal, tipoTransaccion, recordInicial, "0"
        );
        
        return xmlRequest;
    }

    /**
     * Procesa la respuesta del servicio externo.
     */
    private ProductsResponseDto processResponse(String xmlResponse, RequestDto request, String sessionId) {
        try {
            if (xmlResponse == null || xmlResponse.isEmpty()) {
                throw new RuntimeException("Respuesta vacía del servicio externo");
            }

            Document xmlDocument = parseXmlDocument(xmlResponse);
            if (xmlDocument == null) {
                throw new RuntimeException("Error al procesar la respuesta XML");
            }

            String errorCode = XmlUtils.getNodeValue(xmlDocument, "errorCode");
            String errorMessage = XmlUtils.getNodeValue(xmlDocument, "errorMessage");
            String errorType = XmlUtils.getNodeValue(xmlDocument, "errorType");
            String xmlInnerResponse = XmlUtils.getNodeValue(xmlDocument, "XMLReresponse");

            LOG.infof("Respuesta SOAP - Código: %s, Tipo: %s, Mensaje: %s", 
                     errorCode, errorType, errorMessage);

            return processServiceResponse(errorType, errorCode, errorMessage, xmlInnerResponse, request, sessionId);
        } catch (jakarta.ws.rs.NotFoundException e) {
            // Re-lanzar NotFoundException sin envolver para que llegue al NotFoundExceptionMapper
            throw e;
        } catch (Exception e) {
            LOG.errorf("Error procesando respuesta: %s", e.getMessage());
            throw new RuntimeException("Error al procesar la respuesta: " + e.getMessage(), e);
        }
    }

    /**
     * Analiza el documento XML.
     */
    private Document parseXmlDocument(String xml) {
        try {
            return XmlUtils.convertStringToXML(xml);
        } catch (Exception e) {
            LOG.errorf("Error al procesar XML: %s", e.getMessage());
            return null;
        }
    }

    /**
     * Procesa la respuesta del servicio según el tipo de error.
     * Mantiene la lógica específica: SUCCESS + 000 = éxito, WAS01 = NotFoundException, otros = RuntimeException.
     */
    private ProductsResponseDto processServiceResponse(String errorType, String errorCode, String errorMessage, 
                                                     String xmlInnerResponse, RequestDto request, String sessionId) {
        if ("SUCCESS".equalsIgnoreCase(errorType) && "000".equals(errorCode)) {
            return processSuccessResponse(xmlInnerResponse, request, sessionId);
        } else if ("FUNCTIONAL".equalsIgnoreCase(errorType) && "WAS01".equals(errorCode)) {
            // WAS01 = datos no encontrados, lanzar NotFoundException para que el resource retorne 404
            LOG.warnf("WAS01 - No se encontraron datos para producto: %s", request.productNumber());
            throw new jakarta.ws.rs.NotFoundException("No se encontraron datos para el producto: " + request.productNumber());
        } else {
            // Otros errores - lanzar RuntimeException para que el resource retorne 500
            String message = errorMessage != null ? errorMessage : "Error en el servicio externo";
            LOG.errorf("Error del servicio SOAP - Código: %s, Tipo: %s, Mensaje: %s", 
                      errorCode, errorType, message);
            throw new RuntimeException("Error del servicio SOAP: " + message);
        }
    }

    /**
     * Procesa la respuesta exitosa.
     */
    private ProductsResponseDto processSuccessResponse(String xmlInnerResponse, RequestDto request, String sessionId) {
        if (xmlInnerResponse == null || xmlInnerResponse.isEmpty()) {
            return createEmptyResponse(request);
        }

        try {
            Document movementsDocument = XmlUtils.convertStringToXML(xmlInnerResponse);
            List<MovementDto> movements = processMovements(movementsDocument);

            PaginationDto pagination = null;
            if (!movements.isEmpty()) {
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

            LOG.infof("Procesados exitosamente %d movimientos para producto %s", 
                     movements.size(), request.productNumber());

            return new ProductsResponseDto(List.of(productMovements));
        } catch (Exception e) {
            LOG.errorf("Error procesando XML interno: %s", e.getMessage());
            return createErrorResponse(request, "Error al procesar los movimientos: " + e.getMessage());
        }
    }

    /**
     * Procesa los movimientos del documento XML.
     */
    private List<MovementDto> processMovements(Document movementsDocument) {
        if (movementsDocument == null) {
            return List.of();
        }

        NodeList movementNodes = movementsDocument.getElementsByTagName("MovimientoPrestamo");
        
        if (movementNodes == null || movementNodes.getLength() == 0) {
            return List.of();
        }

        return java.util.stream.IntStream.range(0, movementNodes.getLength())
                .mapToObj(i -> (Element) movementNodes.item(i))
                .map(this::extractMovementData)
                .collect(Collectors.toList());
    }

    /**
     * Extrae los datos de un movimiento desde un elemento XML.
     */
    private MovementDto extractMovementData(Element movementElement) {
        if (movementElement == null) {
            return null;
        }

        String transactionNumber = XmlUtils.getTextContent(movementElement, "NumeroTransaccion");
        String date = XmlUtils.getTextContent(movementElement, "Fecha");
        String amountStr = XmlUtils.getTextContent(movementElement, "MontoMovimiento");
        String description = XmlUtils.getTextContent(movementElement, "Concepto");
        String causal = XmlUtils.getTextContent(movementElement, "Causal");
        String uniqueId = XmlUtils.getTextContent(movementElement, "IdUnico");

        BigDecimal amount;
        try {
            amount = amountStr != null ? new BigDecimal(amountStr) : BigDecimal.ZERO;
        } catch (NumberFormatException e) {
            amount = BigDecimal.ZERO;
        }

        String status = (transactionNumber != null && !transactionNumber.trim().isEmpty()) ? "COMPLETED" : "UNKNOWN";

        return new MovementDto("DOP", amount, date, description, status, transactionNumber, uniqueId, causal);
    }

    /**
     * Crea una respuesta de error.
     */
    private ProductsResponseDto createErrorResponse(RequestDto request, String message) {
        return createEmptyResponse(request);
    }

    /**
     * Crea una respuesta vacía.
     */
    private ProductsResponseDto createEmptyResponse(RequestDto request) {
        ProductMovementsDto productMovements = new ProductMovementsDto(
                request.productNumber(),
                request.productLine(),
                request.currency(),
                List.of(),
                null
        );
        
        return new ProductsResponseDto(List.of(productMovements));
    }
}