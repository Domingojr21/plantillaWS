package com.banreservas.services.contracts;

/**
 * Servicio para operaciones de utilidades de fecha.
 * Define el contrato para cálculos de fecha que replican 
 * las funciones ESQL originales.
 * 
 * @author Integration System
 * @since 2025-07-21
 * @version 1.0
 */
public interface IDateUtilService {

    /**
     * Calcula una fecha que es un número específico de meses antes de la fecha actual.
     * 
     * @param months El número de meses a restar de la fecha actual
     * @return Cadena representando la fecha calculada en formato ISO (yyyy-MM-dd'T'00:00:00)
     */
    String calculatePreviousDateByMonths(int months);

     /**
     * Obtiene la fecha actual en el formato requerido por el servicio SOAP.
     * 
     * @return Cadena representando la fecha actual en formato ISO (yyyy-MM-dd'T'00:00:00)
     */
    String getCurrentDateFormatted();
}