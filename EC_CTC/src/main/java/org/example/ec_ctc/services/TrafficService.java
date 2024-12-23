package org.example.ec_ctc.services;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.example.ec_ctc.models.WeatherResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class TrafficService {

    @Value("${openweather.api-key}")
    private String apiKey;

    @Value("${openweather.endpoint}")
    private String endpoint;

    private final RestTemplate restTemplate = new RestTemplate();


    @PostConstruct
    public void init() {
        log.info("TrafficService initialized with endpoint: {}", endpoint);
    }
    public String getTrafficStatus(String city) {
        String url = String.format("%s?q=%s&appid=%s&units=metric", endpoint, city, apiKey);

        System.out.println("TRAFIC STATUS");
        try {
            WeatherResponse response = restTemplate.getForObject(url, WeatherResponse.class);
            if (response != null && response.getMain().getTemp() < 0) {
                return "KO"; // Tráfico no viable
            }
            return "OK"; // Tráfico viable
        } catch (Exception e) {
            return "ERROR: " + e.getMessage(); // Manejo de errores
        }
    }
}
