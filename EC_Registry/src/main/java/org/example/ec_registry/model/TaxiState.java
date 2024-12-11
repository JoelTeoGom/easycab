package org.example.ec_registry.model;

public enum TaxiState {
    IDLE,                    // Esperando un servicio
    ASSIGNED,                // Servicio asignado, en camino para recoger al cliente
    EN_ROUTE_TO_PICKUP,      // En camino a la ubicación del cliente
    PICKUP,                  // Ha llegado al cliente para recogerlo
    EN_ROUTE_TO_DESTINATION, // Transportando al cliente hacia el destino
    STOPPED,                 // Detenido por incidencia o instrucción de CENTRAL
    DESTINATION_REACHED,     // Ha llegado al destino del cliente
    RETURNING_TO_BASE        // Volviendo a la base tras finalizar el servicio
}
