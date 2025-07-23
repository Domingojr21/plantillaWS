package com.banreservas.services.implementations;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.banreservas.services.contracts.IDateUtilService;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Implementación del servicio de utilidades de fecha que proporciona cálculos de fecha
 * replicando la lógica exacta de las funciones ESQL originales.
 * 
 * @author Sistema de Integración
 * @since 2025-07-22
 * @version 1.0
 */
@ApplicationScoped
public class DateUtilService implements IDateUtilService {

    // Formato solo para fecha (yyyy-MM-dd) - para XMLGregorianCalendar
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    // Formato completo con tiempo - para logging/debugging
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'00:00:00");

    @Override
    public String calculatePreviousDateByMonths(int months) {
        try {
            LocalDateTime currentDate = LocalDateTime.now();
            LocalDateTime calculatedDate = currentDate.minusMonths(months);
            
            // Retornamos solo la fecha (yyyy-MM-dd) para facilitar la conversión a XMLGregorianCalendar
            String result = calculatedDate.format(DATE_FORMATTER);
            
            Log.debugf("Fecha anterior calculada por %d meses: %s -> %s", 
                      months, currentDate.format(DATE_FORMATTER), result);
            
            return result;
        } catch (Exception e) {
            Log.errorf(e, "Error calculando fecha anterior por %d meses: %s", months, e.getMessage());
            throw new RuntimeException("Error en cálculo de fecha", e);
        }
    }

    @Override
    public String getCurrentDateFormatted() {
        try {
            LocalDateTime currentDate = LocalDateTime.now();
            
            // Retornamos solo la fecha (yyyy-MM-dd) para facilitar la conversión a XMLGregorianCalendar
            String result = currentDate.format(DATE_FORMATTER);
            
            Log.debugf("Fecha actual formateada: %s", result);
            
            return result;
        } catch (Exception e) {
            Log.errorf(e, "Error formateando fecha actual: %s", e.getMessage());
            throw new RuntimeException("Error en formateo de fecha", e);
        }
    }
    
    /**
     * Método adicional para obtener fecha con formato completo (con tiempo) para logging.
     * 
     * @return Fecha actual en formato yyyy-MM-dd'T'00:00:00
     */
    public String getCurrentDateFullFormat() {
        LocalDateTime currentDate = LocalDateTime.now();
        return currentDate.format(ISO_FORMATTER);
    }
    
    /**
     * Método adicional para obtener fecha anterior con formato completo (con tiempo) para logging.
     * 
     * @param months Cantidad de meses hacia atrás
     * @return Fecha calculada en formato yyyy-MM-dd'T'00:00:00
     */
    public String calculatePreviousDateByMonthsFullFormat(int months) {
        LocalDateTime currentDate = LocalDateTime.now();
        LocalDateTime calculatedDate = currentDate.minusMonths(months);
        return calculatedDate.format(ISO_FORMATTER);
    }
}