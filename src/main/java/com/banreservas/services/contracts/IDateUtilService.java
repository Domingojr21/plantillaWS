package com.banreservas.services.contracts;

/**
 * Service interface for date utility operations.
 * This interface defines the contract for date calculations
 * that replicate the ESQL date functions used in the original
 * integration message processing.
 * 
 * @author Integration System
 * @since 2025-07-21
 * @version 1.0
 */
public interface IDateUtilService {

    /**
     * Calculates a date that is a specified number of months before the current date.
     * This method replicates the exact logic from the ESQL function:
     * Calcular_Ultima_Fecha_MesesP(IN _Meses CHARACTER)
     * 
     * @param months The number of months to subtract from the current date
     * @return A string representing the calculated date in ISO format (yyyy-MM-dd'T'00:00:00)
     */
    String calculatePreviousDateByMonths(int months);

    /**
     * Gets the current date in the format required by the SOAP service.
     * This method formats the current timestamp to match the expected
     * format for the fechaFinal parameter.
     * 
     * @return A string representing the current date in ISO format (yyyy-MM-dd'T'00:00:00)
     */
    String getCurrentDateFormatted();
}