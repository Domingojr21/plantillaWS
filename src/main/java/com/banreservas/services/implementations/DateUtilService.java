package com.banreservas.services.implementations;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.banreservas.services.contracts.IDateUtilService;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Implementación del servicio de utilidades de fecha.
 * Replica exactamente la lógica del ESQL para el cálculo de fechas:
 * - fechaFinal: CURRENT_TIMESTAMP (fecha actual)
 * - fechaInicial: fecha actual menos X meses (función Calcular_Ultima_Fecha_MesesP)
 * 
 * @author Consultor Domingo Ruiz - C-DJruiz@banreservas.com
 * @since 2025-07-22
 * @version 1.0
 */
@ApplicationScoped
public class DateUtilService implements IDateUtilService {

    // Formato exacto como en el ESQL: 'yyyy-MM-dd''T00:00:00'''
    private static final DateTimeFormatter ESQL_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'00:00:00");

    @Override
    public String calculatePreviousDateByMonths(int months) {
        try {
            // Replica exactamente la función Calcular_Ultima_Fecha_MesesP del ESQL:
            // CAST((CURRENT_TIMESTAMP - CAST(_Meses as INTERVAL MONTH )) AS CHARACTER FORMAT 'yyyy-MM-dd''T00:00:00''')
            LocalDateTime currentDate = LocalDateTime.now();
            LocalDateTime calculatedDate = currentDate.minusMonths(months);
            return calculatedDate.format(ESQL_FORMAT);
        } catch (Exception e) {
            throw new RuntimeException("Error calculando fecha anterior", e);
        }
    }

    @Override
    public String getCurrentDateFormatted() {
        try {
            // Replica exactamente el ESQL:
            // CAST(CURRENT_TIMESTAMP AS CHARACTER FORMAT 'yyyy-MM-dd''T00:00:00''')
            LocalDateTime currentDate = LocalDateTime.now();
            return currentDate.format(ESQL_FORMAT);
        } catch (Exception e) {
            throw new RuntimeException("Error formateando fecha actual", e);
        }
    }
}