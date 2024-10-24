package org.example.ec_central.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class MappingUtils {
    private final static ObjectMapper objectMapper = new ObjectMapper();

    public static<T> T map(Object source, Class<T> target) {
        return objectMapper.convertValue(source, target);
    }

    public static<T> T mapFromString(String source, Class<T> target) {
        try {
            return objectMapper.readValue(source, target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
