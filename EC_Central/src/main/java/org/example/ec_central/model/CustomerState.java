package org.example.ec_central.model;

public enum CustomerState {
    IDLE,                    // Cliente sin servicio solicitado
    REQUESTING,              // Solicitando un servicio a CENTRAL
    WAITING_FOR_TAXI,        // Esperando la llegada del taxi asignado
    IN_TRANSIT,              // En camino al destino dentro del taxi
    SERVICE_COMPLETED        // Servicio completado, cliente en destino
}