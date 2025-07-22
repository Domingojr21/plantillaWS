package com.banreservas.services.implementations;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.banreservas.services.contracts.IDateUtilService;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Implementation of date utility service that provides date calculations
 * replicating the exact logic from the original ESQL functions.
 * 
 * This service migrates the date calculation logic from the ESQL function:
 * Calcular_Ultima_Fecha_MesesP which was used to calculate date ranges
 * for loan movement queries.
 * 
 * @author Integration System
 * @since 2025-07-21
 * @version 1.0
 */
@ApplicationScoped
public class DateUtilService implements IDateUtilService {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'00:00:00");

    /**
     * {@inheritDoc}
     * 
     * This method replicates the exact logic from the ESQL function:
     * CREATE FUNCTION Calcular_Ultima_Fecha_MesesP(IN _Meses CHARACTER) RETURNS CHARACTER
     * BEGIN
     *     DECLARE FECHA_CALCULADA CHARACTER '';
     *     SET FECHA_CALCULADA = CAST((CURRENT_TIMESTAMP - CAST(_Meses as INTERVAL MONTH )) AS CHARACTER FORMAT 'yyyy-MM-dd''T00:00:00''' );
     *     RETURN FECHA_CALCULADA;
     * END;
     */
    @Override
    public String calculatePreviousDateByMonths(int months) {
        LocalDateTime currentDate = LocalDateTime.now();
        LocalDateTime calculatedDate = currentDate.minusMonths(months);
        return calculatedDate.format(ISO_FORMATTER);
    }

    /**
     * {@inheritDoc}
     * 
     * Returns the current timestamp formatted to match the SOAP service requirements.
     * This replicates the ESQL: CAST(CURRENT_TIMESTAMP AS CHARACTER FORMAT 'yyyy-MM-dd''T00:00:00''')
     */
    @Override
    public String getCurrentDateFormatted() {
        LocalDateTime currentDate = LocalDateTime.now();
        return currentDate.format(ISO_FORMATTER);
    }
}