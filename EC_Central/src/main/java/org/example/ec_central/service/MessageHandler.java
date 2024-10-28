package org.example.ec_central.service;

import org.springframework.stereotype.Component;

@Component
public class MessageHandler {
    // Definición de constantes para los mensajes de control
    public static final String ACK = "ACK";
    public static final String NACK = "NACK";
    public static final String ENQ = "ENQ";

    private static final char STX = 0x02; // Start of Text
    private static final char ETX = 0x03; // End of Text
    private static final char FIELD_SEPARATOR = '#'; // Separador de campos


    /**
     * Construye un mensaje de solicitud (REQUEST) con los campos dados y calcula el LRC.
     *
     * @param operationCode El código de operación.
     * @param fields        Los campos que forman parte del mensaje de solicitud.
     * @return El mensaje formateado con STX, ETX y LRC.
     */
    public String buildRequest(String operationCode, String... fields) {
        StringBuilder messageBuilder = new StringBuilder(operationCode);

        // Unir los campos con el separador de campo (FIELD_SEPARATOR)
        for (String field : fields) {
            messageBuilder.append(FIELD_SEPARATOR).append(field);
        }

        // Calcular el LRC
        char lrc = calculateLRC(messageBuilder.toString());

        // Formatear el mensaje completo con STX, ETX y LRC
        return STX + messageBuilder.toString() + ETX + lrc;
    }

    /**
     * Calcula el LRC (Longitudinal Redundancy Check) de un mensaje usando XOR byte a byte.
     *
     * @param data El mensaje sobre el cual calcular el LRC.
     * @return El carácter LRC.
     */
    private char calculateLRC(String data) {
        char lrc = 0;
        for (char c : data.toCharArray()) {
            lrc ^= c; // Operación XOR para calcular el LRC
        }
        return lrc;
    }


    /**
     * Valida si la respuesta del servidor tiene un formato válido y si el LRC es correcto.
     *
     * @param message El mensaje completo recibido (incluyendo STX, ETX y LRC).
     * @return true si el mensaje es válido, false en caso contrario.
     */
    public boolean isValidMessage(String message) {
        // Verificar que el mensaje comience con STX y termine con ETX + LRC
        if (message.charAt(0) == STX && message.charAt(message.length() - 2) == ETX) {
            // Extraer el contenido del mensaje (sin STX, ETX ni LRC)
            String data = message.substring(1, message.indexOf(ETX));
            char receivedLRC = message.charAt(message.length() - 1);

            // Calcular el LRC del contenido y compararlo con el recibido
            return receivedLRC == calculateLRC(data);
        }
        return false;
    }

    /**
     * Construye una respuesta de "ACK" o "NACK".
     *
     * @param isAck true para ACK, false para NACK.
     * @return "ACK" o "NACK".
     */
    public String buildAck(boolean isAck) {
        String data = isAck ? "ACK" : "NACK";
        char lrc = calculateLRC(data);
        return STX + data + ETX + lrc; // Construye el mensaje con STX, DATA, ETX y LRC
    }

    /**
     * Valida si el mensaje es un ACK.
     *
     * @param message El mensaje recibido.
     * @return true si el mensaje es "ACK", false si es "NACK".
     */
    public boolean isAck(String message) {
        return "ACK".equals(message);
    }

    /**
     * Valida el formato del mensaje de solicitud.
     *
     * @param request El mensaje de solicitud.
     * @return true si el formato es válido, false en caso contrario.
     */
    public boolean isValidRequest(String request) {
        if (request.length() < 4) {
            return false;
        }

        if (request.charAt(0) == STX && request.charAt(request.length() - 2) == ETX) {
            String data = request.substring(1, request.length() - 2);
            char receivedLRC = request.charAt(request.length() - 1);

            return receivedLRC == calculateLRC(data);
        }
        return false;
    }

    public boolean isValidAuthentication(String message) {
        if (isValidMessage(message)) {
            String data = extractData(message);
            String[] parts = data.split("#");
            return parts.length == 3 && "AUTH".equals(parts[0]) && "token123".equals(parts[2]);
        }
        return false;
    }

    public String extractData(String message) {
        return message.substring(1, message.indexOf(ETX));
    }

    /**
     * Extrae el destino del mensaje de solicitud.
     *
     * @param request El mensaje de solicitud.
     * @return El destino extraído o null si no es válido.
     */
    public String extractDestination(String request) {
        if (isValidRequest(request)) {
            String data = request.substring(1, request.indexOf(ETX));
            String[] parts = data.split(String.valueOf(FIELD_SEPARATOR));
            if (parts.length > 1) {
                return parts[1];
            }
        }
        return null;
    }
}
