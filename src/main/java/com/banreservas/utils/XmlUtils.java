package com.banreservas.utils;

import java.io.StringReader;
import java.text.MessageFormat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;

/**
 * Utilidad para procesamiento de XML y construcción de mensajes SOAP.
 * Proporciona métodos para convertir cadenas XML a Document, extraer valores de nodos
 * y construir requests SOAP.
 * 
 * @author Consultor Domingo Ruiz - C-DJruiz@banreservas.com
 * @since 2025-07-22
 * @version 1.0
 */
public class XmlUtils {

    /**
     * Convierte una cadena XML en un objeto Document.
     * 
     * @param xmlString Cadena XML a convertir
     * @return Documento XML parseado
     * @throws RuntimeException Si ocurre un error en la conversión
     */
    public static Document convertStringToXML(String xmlString) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new InputSource(new StringReader(xmlString)));
        } catch (Exception e) {
            throw new RuntimeException("Error al convertir XML", e);
        }
    }

    /**
     * Deserializa un XML en un objeto de la clase especificada.
     * 
     * @param xml   Cadena XML a deserializar
     * @param clazz Clase de destino
     * @param <T>   Tipo de la clase destino
     * @return Objeto deserializado
     * @throws RuntimeException Si ocurre un error en la conversión
     */
    @SuppressWarnings("unchecked")
    public static <T> T parseXml(String xml, Class<T> clazz) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return (T) unmarshaller.unmarshal(new StringReader(xml));
        } catch (Exception e) {
            throw new RuntimeException("Error al convertir XML", e);
        }
    }

    /**
     * Obtiene el valor de un nodo XML dado su nombre.
     * 
     * @param doc     Documento XML
     * @param tagName Nombre del nodo a buscar
     * @return Valor del nodo o null si no existe
     */
    public static String getNodeValue(Document doc, String tagName) {
        NodeList nodeList = doc.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return null;
    }

    /**
     * Obtiene el contenido de texto de un elemento hijo específico.
     * 
     * @param parent  Elemento padre
     * @param tagName Nombre del tag hijo
     * @return Contenido de texto del elemento o null si no existe
     */
    public static String getTextContent(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList != null && nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return null;
    }

    /**
     * Obtiene el contenido de texto de un elemento hijo específico con valor por defecto.
     * 
     * @param parent       Elemento padre
     * @param tagName      Nombre del tag hijo
     * @param defaultValue Valor por defecto si no existe o está vacío
     * @return Contenido de texto del elemento o valor por defecto
     */
    public static String getTextContentOrDefault(Element parent, String tagName, String defaultValue) {
        String value = getTextContent(parent, tagName);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }

    /**
     * Obtiene el primer nodo hijo con el nombre especificado.
     * 
     * @param parent  Elemento padre
     * @param tagName Nombre del tag a buscar
     * @return Primer nodo encontrado o null
     */
    public static Node getFirstChildByTagName(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList != null && nodeList.getLength() > 0) {
            return nodeList.item(0);
        }
        return null;
    }

    /**
     * Construye un mensaje SOAP para el servicio de movimientos de préstamo.
     * Formato exacto que funciona según el curl de ejemplo.
     * 
     * @param channel Canal de la solicitud
     * @param dateTime Fecha y hora de la solicitud
     * @param terminal Terminal de la solicitud
     * @param user Usuario de la solicitud
     * @param productNumber Número de producto
     * @param cantidad Cantidad de movimientos
     * @param direccion Dirección de consulta
     * @param fechaInicial Fecha inicial de consulta
     * @param fechaFinal Fecha final de consulta
     * @param montoInicial Monto inicial de filtro
     * @param montoFinal Monto final de filtro
     * @param tipo Tipo de transacción
     * @param record Record inicial
     * @param numDoc Número de documento
     * @return XML SOAP formateado exactamente como el curl funcional
     */
    public static String buildSoapRequest(String channel, String dateTime, String terminal, String user,
                                        String productNumber, int cantidad, String direccion, 
                                        String fechaInicial, String fechaFinal, double montoInicial, 
                                        double montoFinal, String tipo, int record, String numDoc) {
        
        // Usar el formato exacto del curl que funciona
        String template = """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ser="http://services.service.brrd.com/">
                    <soapenv:Header/>
                    <soapenv:Body>
                        <ser:movimientosPrestamo>
                            <MovimientosPrestamoRequest>
                                <channel>{0}</channel>
                                <date>{1}</date>
                                <operationName>movimientosPrestamo</operationName>
                                <terminal>{2}</terminal>
                                <user>{3}</user>
                                <cantidad>{4}</cantidad>
                                <direccion>{5}</direccion>
                                <fechaFinal>{6}</fechaFinal>
                                <fechaInicial>{7}</fechaInicial>
                                <montoFinal>{8}</montoFinal>
                                <montoInicial>{9}</montoInicial>
                                <numDoc>{10}</numDoc>
                                <producto>{11}</producto>
                                <record>{12}</record>
                                <tipo>{13}</tipo>
                            </MovimientosPrestamoRequest>
                        </ser:movimientosPrestamo>
                    </soapenv:Body>
                </soapenv:Envelope>
                """;

        return MessageFormat.format(template,
                channel, dateTime, terminal, user, cantidad, direccion,
                fechaFinal, fechaInicial, 
                String.format("%.0f", montoFinal), String.format("%.0f", montoInicial), 
                numDoc, productNumber, record, tipo);
    }
}