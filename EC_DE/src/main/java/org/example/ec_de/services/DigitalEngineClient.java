package org.example.ec_de.services;

import java.io.FileInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyStore;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class DigitalEngineClient {

    private final HttpClient httpClient;
    private final String registryUrl;

    public DigitalEngineClient(String registryUrl) throws Exception {
        this.registryUrl = System.getenv("REGISTRY_URL");
        if (this.registryUrl == null || this.registryUrl.isEmpty()) {
            throw new IllegalArgumentException("REGISTRY_URL no está configurada.");
        }
        System.out.println("Conectando a " + this.registryUrl);
        KeyStore trustStore = KeyStore.getInstance("JKS");
        try (FileInputStream trustStoreStream = new FileInputStream("/client/truststore.jks")) {
            trustStore.load(trustStoreStream, "your_password".toCharArray());
        }

        // Configurar TrustManagerFactory
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);

        // Configurar SSLContext
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

        // Crear HttpClient con SSLContext
        this.httpClient = HttpClient.newBuilder()
                .sslContext(sslContext)
                .build();
    }

    public void registerTaxi(String taxiId) throws Exception {
        URI uri = new URI(registryUrl + "/register");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString("{\"id\": \"" + taxiId + "\"}"))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Respuesta del servidor: " + response.body());
    }


    public void unregisterTaxi(String taxiId) throws Exception {
        URI uri = new URI(registryUrl + "/unregister/" + taxiId);
        System.out.println("Connecting to: " + uri); // Agrega esta línea para depuración
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Respuesta del servidor: " + response.body());
    }

    public void getTaxiStatus(String taxiId) throws Exception {
        URI uri = new URI(registryUrl + "/" + taxiId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Respuesta del servidor: " + response.body());
    }
}
