package org.example.ec_central.service;

import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private static final char STX = 0x02;
    private static final char ETX = 0x03;

    // Método para validar el mensaje de autenticación
    public boolean isValidMessage(String message) {
        if (message.charAt(0) == STX && message.charAt(message.length() - 2) == ETX) {
            String data = message.substring(1, message.indexOf(ETX));
            char receivedLRC = message.charAt(message.length() - 1);
            char calculatedLRC = calculateLRC(data);
            return receivedLRC == calculatedLRC;
        }
        return false;
    }

    // Construir respuesta para enviar al cliente (ACK/NACK)
    public String buildResponse(String response) {
        String data = response;
        char lrc = calculateLRC(data);
        return STX + data + ETX + lrc;
    }

    // Calcular el LRC (Longitud Redundante de Control) como XOR de los bytes
    private char calculateLRC(String data) {
        char lrc = 0;
        for (char c : data.toCharArray()) {
            lrc ^= c;
        }
        return lrc;
    }
}