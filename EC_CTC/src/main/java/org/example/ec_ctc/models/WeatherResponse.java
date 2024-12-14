package org.example.ec_ctc.models;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherResponse {
    private Main main;

    public void setMain(Main main) {
        this.main = main;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Main {
        private double temp;

        public double getTemp() {
            return temp;
        }

        public void setTemp(double temp) {
            this.temp = temp;
        }
    }
}
