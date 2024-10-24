package org.example.ec_s;

import lombok.extern.slf4j.Slf4j;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

@Slf4j
public class SensorSocketClient {

    private String host;
    private int port;
    private Socket socket;
    private PrintWriter out;  // Mantener una referencia a PrintWriter

    public SensorSocketClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.establishConnection();
    }

    // Establecer conexión
    private void establishConnection() {
        try {
            this.socket = new Socket(host, port);
            this.out = new PrintWriter(new OutputStreamWriter(this.socket.getOutputStream()), true);
            log.info("Conexión establecida con el servidor en {}:{}", host, port);
        } catch (Exception e) {
            log.error("Error al establecer la conexión con el servidor en {}:{}", host, port);
            e.printStackTrace();
        }
    }

    // Enviar datos del sensor
    public void sendSensorData(String data) {
        try {
            // Verificar si el socket está cerrado o no es válido
            if (this.socket == null || this.socket.isClosed()) {
                log.info("Conexión cerrada, intentando reconectar...");
                establishConnection();  // Intentar reconectar
            }

            // Enviar los datos reutilizando PrintWriter
            out.println(data);  // Usar PrintWriter que ya está abierto
            log.info("Datos enviados: {}", data);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Cerrar el socket y PrintWriter
    public void close() {
        try {
            if (this.out != null) {
                this.out.close();  // Cerrar el PrintWriter
            }
            if (this.socket != null && !this.socket.isClosed()) {
                this.socket.close();
                log.warn("Socket cerrado.");
            }
        } catch (Exception e) {
            log.error("Error al cerrar el socket.");
            e.printStackTrace();
        }
    }
}