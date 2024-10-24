package org.example.ec_de.model;

public enum SensorStatus {
    OK("OK"),
    KO("KO");

    private final String value;

    SensorStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}