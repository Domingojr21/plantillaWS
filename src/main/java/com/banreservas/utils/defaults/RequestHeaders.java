package com.banreservas.utils.defaults;

import java.util.List;

/**
 * Clase que define las constantes utilizadas para los encabezados HTTP en las
 * solicitudes.
 * Esta clase contiene las constantes correspondientes a los encabezados comunes
 * que se utilizan
 * en las solicitudes HTTP dentro de la aplicación, tales como identificadores
 * de sesión, consumidor,
 * usuario, entre otros. También proporciona una lista con todos los encabezados
 * definidos.
 * 
 * @author Ing. Victor Diaz - vjdiaz@banreservas.com
 * @since 06-02-2025
 * @version 1.0
 */
public class RequestHeaders {

    /**
     * Nombre del encabezado que representa el ID de sesión.
     */
    public static final String SESSION_ID = "sessionId";

    /**
     * Nombre del encabezado que representa el ID del consumidor.
     */
    public static final String CHANNEL = "channel";

    /**
     * Nombre del encabezado que representa al usuario de la solicitud.
     */
    public static final String USER = "user";

    /**
     * Nombre del encabezado que representa la fecha y hora de la solicitud.
     */
    public static final String FECHA_HORA = "dateTime";

    /**
     * Nombre del encabezado que representa el terminal desde el cual se realiza la
     * solicitud.
     */
    public static final String TERMINAL = "terminal";

    /**
     * Nombre del encabezado que representa la operación asociada a la solicitud.
     */
    public static final String OPERATION = "operation";

    /**
     * Lista de todos los encabezados definidos en esta clase.
     * 
     * @see #SESSION_ID
     * @see #CHANNEL
     * @see #USER
     * @see #FECHA_HORA
     * @see #TERMINAL
     * @see #OPERATION
     */
    public static final List<String> HEADERS = List.of(
            SESSION_ID, CHANNEL, USER, FECHA_HORA, TERMINAL, OPERATION);
}
