package org.example.ec_de.model;

/**
 * Enum representing the status of a sensor.
 */
public enum SensorStatus {
    /**
     * Status indicating the sensor is operational.
     */
    OK("OK"),

    /**
     * Status indicating the sensor is not operational.
     */
    KO("KO");

    /**
     * The string value representing the sensor status.
     */
    private final String value;

    /**
     * Constructor for SensorStatus enum.
     *
     * @param value the string value representing the sensor status
     */
    SensorStatus(String value) {
        this.value = value;
    }

    /**
     * Gets the string value of the sensor status.
     *
     * @return the string value of the sensor status
     */
    public String getValue() {
        return value;
    }
}