package org.example.logginglibrary.logginglibrary.config;


import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.util.StatusPrinter;

import java.io.InputStream;

import org.slf4j.LoggerFactory;

@Configuration
@ConditionalOnClass(LoggerContext.class)
public class LoggingLibraryAutoConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(LoggingLibraryAutoConfiguration.class);
    @PostConstruct
    public void configureLogback() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        try (InputStream configStream = getClass().getClassLoader().getResourceAsStream("logback-spring.xml")) {
            if (configStream != null) {
                JoranConfigurator configurator = new JoranConfigurator();
                configurator.setContext(context);
                context.reset(); // Resetea la configuración actual de Logback
                configurator.doConfigure(configStream); // Aplica la configuración desde el archivo XML
            } else {
                logger.warn("No se encontró el archivo de configuración de Logback.");
            }
        } catch (Exception e) {
            logger.error("Error al configurar Logback.", e);
            StatusPrinter.printInCaseOfErrorsOrWarnings(context);
        }
    }
}