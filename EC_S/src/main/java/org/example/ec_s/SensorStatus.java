package org.example.ec_s;

/**
 * Enum representing the status of a sensor.
 */
public enum SensorStatus {
    /**
     * Status indicating the sensor is operating correctly.
     */
    OK("OK"),

    /**
     * Status indicating the sensor is not operating correctly.
     */
    KO("KO");

    /**
     * The string value representing the status.
     */
    private final String value;

    /**
     * Constructor for SensorStatus.
     *
     * @param value the string value representing the status
     */
    SensorStatus(String value) {
        this.value = value;
    }

    /**
     * Gets the string value of the status.
     *
     * @return the string value of the status
     */
    public String getValue() {
        return value;
    }
}