package org.example;

import java.net.*;

// Main class to start the HTTP server
public final class ServidorWeb {
    public static void main(String[] args) throws Exception {
        int puerto = 6789; // Port number where the server listens
        ServerSocket socketBienvenida = new ServerSocket(puerto); // Create server socket

        while (true) {
            // Wait for client connections
            Socket socketConexion = socketBienvenida.accept();

            // Handle each request in a new thread
            SolicitudHttp solicitud = new SolicitudHttp(socketConexion);
            Thread hilo = new Thread(solicitud);
            hilo.start();
        }
    }
}

