package org.example.ec_de.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

/**
 * Utility class for mapping objects to JSON strings and vice versa.
 */
@Component
public class MappingUtils {
    /**
     * ObjectMapper instance for JSON processing.
     */
    private final static ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Maps an object to its JSON string representation.
     *
     * @param source the object to map
     * @return the JSON string representation of the object
     * @throws RuntimeException if there is an error during JSON processing
     */
    public static String map(Object source) {
        try {
            return objectMapper.writeValueAsString(source);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Maps a JSON string to an object of the specified class.
     *
     * @param source the JSON string to map from
     * @param target the class of the object to map to
     * @param <T>    the type of the object
     * @return the object mapped from the JSON string
     * @throws RuntimeException if there is an error during JSON processing
     */
    public static <T> T mapFromString(String source, Class<T> target) {
        try {
            return objectMapper.readValue(source, target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}