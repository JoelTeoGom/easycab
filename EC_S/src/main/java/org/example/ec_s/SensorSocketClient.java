package org.example.ec_s;

import lombok.extern.slf4j.Slf4j;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Client class for handling socket communication with a sensor server.
 */
@Slf4j
public class SensorSocketClient {

    /**
     * The host address of the sensor server.
     */
    private String host;

    /**
     * The port number of the sensor server.
     */
    private int port;

    /**
     * The socket for the connection to the sensor server.
     */
    private Socket socket;

    /**
     * The PrintWriter for sending data to the sensor server.
     */
    private PrintWriter out;  // Mantener una referencia a PrintWriter

    /**
     * Constructor for SensorSocketClient.
     *
     * @param host the host address of the sensor server
     * @param port the port number of the sensor server
     */
    public SensorSocketClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.establishConnection();
    }

    /**
     * Establishes a connection to the sensor server.
     */
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

    /**
     * Sends sensor data to the sensor server.
     *
     * @param data the sensor data to send
     */
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

    /**
     * Closes the socket and PrintWriter.
     */
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