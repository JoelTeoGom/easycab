package org.example.ec_central.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
@AllArgsConstructor
public class ConnectedTaxis {
    private final ConcurrentHashMap<String, Socket> connectedTaxis = new ConcurrentHashMap<>();

    public boolean isTaxiConnected(int taxiId) {
        Socket taxiSocket = connectedTaxis.get(taxiId);
        return taxiSocket != null && !taxiSocket.isClosed();
    }
}
