package com.banreservas.services.implementations;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Retry;

import com.banreservas.dtos.inbound.MovementDto;
import com.banreservas.dtos.inbound.PaginationDto;
import com.banreservas.dtos.inbound.ProductMovementsDto;
import com.banreservas.dtos.inbound.ProductsResponseDto;
import com.banreservas.dtos.inbound.RequestDto;
import com.banreservas.dtos.inbound.ResponseDto;
import com.banreservas.dtos.outbound.SoapMovementDto;
import com.banreservas.dtos.outbound.SoapResponseDto;
import com.banreservas.utils.SoapXmlWrapper;
import com.banreservas.utils.BuildJsonConstructLogAppender;
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

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedMap;
import java.math.BigDecimal;

/**
 * Implementación del servicio de movimientos de préstamo.
 * Maneja la lógica de negocio para recuperar movimientos de préstamo
 * desde el servicio SOAP backend.
 * 
 * @author Integration System
 * @since 2025-07-21
 * @version 1.0
 */
@ApplicationScoped
public class LoanMovementsService implements ILoanMovementsService {

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
     * Obtener el cliente SOAP de manera lazy para evitar problemas de inyección en nativo
     */
   // URL del servicio SOAP
    @ConfigProperty(name = "soap.endpoint.url", defaultValue = "http://172.22.30.34:9082/DEV/API/2.0/MovimientosPrestamo/service")
    private String soapEndpointUrl;

    /**
     * Crear cliente SOAP programáticamente sin usar CDI
     */
    private ImovimientosPrestamoRequest createSoapClient() {
        try {
            Log.info("Creando cliente SOAP programáticamente");
            
            // Crear el servicio usando JAX-WS estándar
            java.net.URL wsdlUrl = new java.net.URL(soapEndpointUrl + "?wsdl");
            javax.xml.namespace.QName serviceName = new javax.xml.namespace.QName(
                "http://services.service.brrd.com/", "service");
            
            jakarta.xml.ws.Service service = jakarta.xml.ws.Service.create(wsdlUrl, serviceName);
            ImovimientosPrestamoRequest client = service.getPort(ImovimientosPrestamoRequest.class);
            
            // Configurar el endpoint
            jakarta.xml.ws.BindingProvider bindingProvider = (jakarta.xml.ws.BindingProvider) client;
            bindingProvider.getRequestContext().put(
                jakarta.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY, 
                soapEndpointUrl
            );
            
            // Configurar timeouts
            bindingProvider.getRequestContext().put("com.sun.xml.ws.connect.timeout", 30000);
            bindingProvider.getRequestContext().put("com.sun.xml.ws.request.timeout", 15000);
            
            Log.info("Cliente SOAP creado exitosamente");
            return client;
            
        } catch (Exception e) {
            Log.errorf(e, "Error creando cliente SOAP: %s", e.getMessage());
            throw new RuntimeException("No se pudo crear el cliente SOAP", e);
        }
    }

    @Override
   @Retry(maxRetries = 1, delay = 1000, retryOn = { RuntimeException.class })
@CircuitBreaker(requestVolumeThreshold = 2, failureRatio = 1.0, delay = 5000)
public Uni<ProductsResponseDto> getLoanMovements(RequestDto request, MultivaluedMap<String, String> headers)
        throws JsonProcessingException {

    String sessionId = headers.getFirst("sessionId");
    
    Log.infof("[SessionId: %s] Procesando movimientos para producto: %s, línea: %s, moneda: %s", 
             sessionId, request.productNumber(), request.productLine(), request.currency());

    try {
        MovimientosPrestamoRequest soapRequest = buildSoapRequest(request, headers);
        
        Log.infof("[SessionId: %s] Request SOAP construido exitosamente", sessionId);
        Log.debugf("[SessionId: %s] Detalles del request SOAP: %s", 
                  sessionId, BuildJsonConstructLogAppender.buildJson(soapRequest));

        return Uni.createFrom().item(() -> {
            try {
                Log.infof("[SessionId: %s] Llamando servicio SOAP para producto: %s", 
                         sessionId, request.productNumber());

                          ImovimientosPrestamoRequest soapClient = createSoapClient();
                   
                
                long tiempoInicio = System.currentTimeMillis();
                ServiceResponse soapResponse = soapClient.movimientosPrestamo(soapRequest);
              long tiempoFin = System.currentTimeMillis();
                
                Log.infof("[SessionId: %s] Llamada SOAP completada en %d ms", 
                         sessionId, (tiempoFin - tiempoInicio));
                
                Log.infof("[SessionId: %s] Respuesta SOAP recibida - Código: %s, Tipo: %s", 
                          sessionId, soapResponse.getErrorCode(), soapResponse.getErrorType());

                if ("000".equals(soapResponse.getErrorCode()) && "SUCCESS".equals(soapResponse.getErrorType())) {
                    Log.infof("[SessionId: %s] Servicio SOAP retornó respuesta exitosa", sessionId);
                    
                    String xmlResponse = soapResponse.getXMLReresponse();
                    Log.infof("[SessionId: %s] XML de respuesta recibido con %d caracteres", 
                              sessionId, xmlResponse != null ? xmlResponse.length() : 0);
                    
                    if (xmlResponse != null && xmlResponse.length() > 500) {
                        Log.debugf("[SessionId: %s] Inicio del XML de respuesta: %s...", 
                                  sessionId, xmlResponse.substring(0, 500));
                    } else {
                        Log.debugf("[SessionId: %s] XML de respuesta completo: %s", sessionId, xmlResponse);
                    }
                    
                    return processSuccessfulResponse(soapResponse, request, sessionId);
                } else {
                    Log.errorf("[SessionId: %s] Error en servicio SOAP - Código: %s, Tipo: %s, Mensaje: %s", 
                              sessionId, soapResponse.getErrorCode(), soapResponse.getErrorType(), soapResponse.getErrorMessage());
                    throw new IllegalArgumentException(String.format("Error del servicio SOAP: %s", soapResponse.getErrorMessage()));
                }

            } catch (Exception e) {
                Log.errorf(e, "[SessionId: %s] Excepción llamando servicio SOAP: %s", sessionId, e.getMessage());
                throw new RuntimeException(String.format("Falla en llamada al servicio SOAP: %s", e.getMessage()), e);
            }
        }).runSubscriptionOn(Infrastructure.getDefaultExecutor());
        
    } catch (Exception e) {
        Log.errorf(e, "[SessionId: %s] Error en getLoanMovements: %s", sessionId, e.getMessage());
        return Uni.createFrom().failure(new RuntimeException(String.format("Error del servicio: %s", e.getMessage()), e));
    }
}
    


    private MovimientosPrestamoRequest buildSoapRequest(RequestDto request, MultivaluedMap<String, String> headers) {
    String sessionId = headers.getFirst("sessionId");
    
    Log.infof("[SessionId: %s] Construyendo request SOAP con configuración - cantidad: %d, dirección: %s, meses atrás: %d", 
              sessionId, cantidadMovimientos, direccionConsulta, mesesAtras);
    
    MovimientosPrestamoRequest soapRequest = new MovimientosPrestamoRequest();

    // Usar valores que funcionan según el SOAP de ejemplo
    // Channel debe ser numérico como en el ejemplo que funciona
    String channel = "31"; // Valor fijo que funciona según el curl de ejemplo
    String dateTime = headers.getFirst("dateTime");
    String terminal = "0.0.0.0"; // Formato IP como en el ejemplo que funciona  
    String user = headers.getFirst("user");
    
    Log.infof("[SessionId: %s] Valores para SOAP - channel: %s, dateTime: %s, terminal: %s, user: %s", 
              sessionId, channel, dateTime, terminal, user);
    
    // Validar que los campos requeridos no sean null
    if (dateTime == null || dateTime.trim().isEmpty()) {
        Log.errorf("[SessionId: %s] Header 'dateTime' es null o vacío", sessionId);
        throw new IllegalArgumentException("Header 'dateTime' es requerido");
    }
    if (user == null || user.trim().isEmpty()) {
        Log.errorf("[SessionId: %s] Header 'user' es null o vacío", sessionId);
        throw new IllegalArgumentException("Header 'user' es requerido");
    }

    soapRequest.setChannel(channel);
    soapRequest.setDate(dateTime);
    soapRequest.setOperationName("movimientosPrestamo");
    soapRequest.setTerminal(terminal);
    soapRequest.setUser(user);

    soapRequest.setCantidad(cantidadMovimientos);
    soapRequest.setDireccion(direccionConsulta);
    
    try {
        // Usar fechas más amplias como en el ejemplo que funciona
        String fechaFinal = "2025-12-30T00:00:00";
        String fechaInicial = "2015-01-11T00:00:00";
        
        Log.infof("[SessionId: %s] Rango de fechas fijo para prueba - Desde: %s Hasta: %s", 
                  sessionId, fechaInicial, fechaFinal);
        
        // Convertir fechas usando método simplificado
        soapRequest.setFechaFinal(convertToXMLGregorianCalendarSimple(fechaFinal, sessionId));
        soapRequest.setFechaInicial(convertToXMLGregorianCalendarSimple(fechaInicial, sessionId));
    } catch (Exception e) {
        Log.errorf(e, "[SessionId: %s] Error convirtiendo fechas a XMLGregorianCalendar: %s", sessionId, e.getMessage());
        throw new RuntimeException("Error convirtiendo fechas a XMLGregorianCalendar", e);
    }
    
    // Usar los mismos valores que en el ejemplo que funciona
    soapRequest.setMontoFinal(999999999999.0);  // Como en el ejemplo
    soapRequest.setMontoInicial(0.0);           // Como en el ejemplo
    soapRequest.setProducto(request.productNumber());
    soapRequest.setRecord(0);                   // Como en el ejemplo
    soapRequest.setTipo(tipoTransaccion);
    
    // Agregar numDoc como en el ejemplo
    soapRequest.setNumDoc("0");                 // Como en el ejemplo

    Log.infof("[SessionId: %s] Request SOAP construido para producto: %s, montos: %.0f-%.0f, tipo: %s", 
              sessionId, request.productNumber(), soapRequest.getMontoInicial(), soapRequest.getMontoFinal(), tipoTransaccion);
    
    // Log completo del request SOAP antes de enviarlo
    Log.infof("[SessionId: %s] Request SOAP completo:", sessionId);
    Log.infof("[SessionId: %s] - Channel: %s", sessionId, soapRequest.getChannel());
    Log.infof("[SessionId: %s] - Date: %s", sessionId, soapRequest.getDate());
    Log.infof("[SessionId: %s] - User: %s", sessionId, soapRequest.getUser());
    Log.infof("[SessionId: %s] - Terminal: %s", sessionId, soapRequest.getTerminal());
    Log.infof("[SessionId: %s] - Operation: %s", sessionId, soapRequest.getOperationName());
    Log.infof("[SessionId: %s] - Producto: %s", sessionId, soapRequest.getProducto());
    Log.infof("[SessionId: %s] - Cantidad: %d", sessionId, soapRequest.getCantidad());
    Log.infof("[SessionId: %s] - Dirección: %s", sessionId, soapRequest.getDireccion());
    Log.infof("[SessionId: %s] - Tipo: %s", sessionId, soapRequest.getTipo());
    Log.infof("[SessionId: %s] - NumDoc: %s", sessionId, soapRequest.getNumDoc());
    Log.infof("[SessionId: %s] - Monto Inicial: %.0f", sessionId, soapRequest.getMontoInicial());
    Log.infof("[SessionId: %s] - Monto Final: %.0f", sessionId, soapRequest.getMontoFinal());
    Log.infof("[SessionId: %s] - Fecha Inicial: %s", sessionId, soapRequest.getFechaInicial());
    Log.infof("[SessionId: %s] - Fecha Final: %s", sessionId, soapRequest.getFechaFinal());

    return soapRequest;
}

/**
 * Método simplificado para convertir fechas en formato string a XMLGregorianCalendar
 */
private XMLGregorianCalendar convertToXMLGregorianCalendarSimple(String dateTimeStr, String sessionId) throws Exception {
    try {
        Log.debugf("[SessionId: %s] Convirtiendo fecha: %s", sessionId, dateTimeStr);
        
        // Parsear la fecha completa: "2025-12-30T00:00:00"
        LocalDate localDate = LocalDate.parse(dateTimeStr.substring(0, 10), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        // Crear XMLGregorianCalendar manualmente para tener control completo
        XMLGregorianCalendar xmlCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar();
        xmlCalendar.setYear(localDate.getYear());
        xmlCalendar.setMonth(localDate.getMonthValue());
        xmlCalendar.setDay(localDate.getDayOfMonth());
        xmlCalendar.setHour(0);
        xmlCalendar.setMinute(0);
        xmlCalendar.setSecond(0);
        
        Log.debugf("[SessionId: %s] Fecha convertida exitosamente: %s -> %s", 
                   sessionId, dateTimeStr, xmlCalendar.toString());
        
        return xmlCalendar;
        
    } catch (Exception e) {
        Log.errorf(e, "[SessionId: %s] Error en conversión de fecha: %s", sessionId, dateTimeStr);
        throw new Exception("Error convirtiendo fecha: " + dateTimeStr + " - " + e.getMessage(), e);
    }
}
private ProductsResponseDto processSuccessfulResponse(ServiceResponse soapResponse, RequestDto request, String sessionId) 
        throws JsonProcessingException {
    
    Log.infof("[SessionId: %s] Procesando respuesta SOAP exitosa", sessionId);
    
    String xmlContent = soapResponse.getXMLReresponse();
    
    if (xmlContent == null || xmlContent.trim().isEmpty()) {
        Log.warnf("[SessionId: %s] Respuesta XML vacía del servicio SOAP", sessionId);
        return createEmptyResponse(request, sessionId);
    }

    try {
        Log.infof("[SessionId: %s] Parseando contenido XML con longitud: %d", sessionId, xmlContent.length());
        
        // Configurar XmlMapper para ignorar propiedades desconocidas
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        xmlMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        
        Log.debugf("[SessionId: %s] XmlMapper configurado para ignorar campos desconocidos", sessionId);
        
        SoapXmlWrapper xmlWrapper = xmlMapper.readValue(xmlContent, SoapXmlWrapper.class);
        SoapResponseDto parsedResponse = xmlWrapper.toRecord();

        Log.infof("[SessionId: %s] Parseo de XML exitoso", sessionId);

        List<MovementDto> movements = mapSoapMovementsToDto(parsedResponse.movements(), sessionId);
        
        PaginationDto pagination = null;
        if (movements != null && !movements.isEmpty()) {
            String lastUniqueId = movements.get(movements.size() - 1).uniqueId();
            pagination = new PaginationDto(lastUniqueId);
            Log.infof("[SessionId: %s] Paginación creada con uniqueId: %s", sessionId, lastUniqueId);
        }

        ProductMovementsDto productMovements = new ProductMovementsDto(
            request.productNumber(),
            request.productLine(), 
            request.currency(),
            movements,
            pagination
        );

        Log.infof("[SessionId: %s] Procesados exitosamente %d movimientos para producto %s", 
                 sessionId, movements != null ? movements.size() : 0, request.productNumber());

        ProductsResponseDto response = new ProductsResponseDto(List.of(productMovements));
        
        Log.debugf("[SessionId: %s] Respuesta final: %s", 
                  sessionId, BuildJsonConstructLogAppender.buildJson(response));

        return response;

    } catch (Exception e) {
        Log.errorf(e, "[SessionId: %s] Error parseando respuesta XML: %s", sessionId, e.getMessage());
        throw new RuntimeException("Error procesando respuesta SOAP", e);
    }
}

  private List<MovementDto> mapSoapMovementsToDto(List<SoapMovementDto> soapMovements, String sessionId) {
    if (soapMovements == null) {
        Log.infof("[SessionId: %s] No hay movimientos para mapear (lista nula)", sessionId);
        return List.of();
    }

    Log.infof("[SessionId: %s] Mapeando %d movimientos SOAP a DTOs", sessionId, soapMovements.size());

    List<MovementDto> result = soapMovements.stream()
        .map(soapMovement -> {
            Log.debugf("[SessionId: %s] Mapeando movimiento - Transacción: %s, Monto: %s, Fecha: %s", 
                      sessionId, soapMovement.transactionNumber(), soapMovement.amount(), soapMovement.date());
            
            // Convertir amount de String a BigDecimal
            BigDecimal amountDecimal;
            try {
                amountDecimal = soapMovement.amount() != null ? 
                    new BigDecimal(soapMovement.amount()) : BigDecimal.ZERO;
            } catch (NumberFormatException e) {
                Log.warnf("[SessionId: %s] Error convirtiendo amount '%s' a BigDecimal, usando 0", 
                         sessionId, soapMovement.amount());
                amountDecimal = BigDecimal.ZERO;
            }
            
            return new MovementDto(
                soapMovement.getCurrency(),        // currency - String
                amountDecimal,                     // amount - BigDecimal
                soapMovement.date(),               // date - String
                soapMovement.description(),        // description - String
                soapMovement.getStatus(),          // status - String
                soapMovement.transactionNumber(),  // transactionNumber - String
                soapMovement.uniqueId(),           // uniqueId - String
                soapMovement.causal()              // causal - String
            );
        })
        .collect(Collectors.toList());

    Log.infof("[SessionId: %s] Mapeados exitosamente %d movimientos", sessionId, result.size());
    return result;
}


    private ProductsResponseDto createEmptyResponse(RequestDto request, String sessionId) {
        Log.infof("[SessionId: %s] Creando respuesta vacía para producto %s", sessionId, request.productNumber());
        
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
