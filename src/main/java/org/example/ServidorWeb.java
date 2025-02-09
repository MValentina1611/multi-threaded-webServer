package org.example;// ServidorWeb.java
import org.example.SolicitudHttp;

import java.net.*;

public final class ServidorWeb {
    public static void main(String[] args) throws Exception {
        int puerto = 6789;
        ServerSocket socketBienvenida = new ServerSocket(puerto);

        while (true) {
            Socket socketConexion = socketBienvenida.accept();
            SolicitudHttp solicitud = new SolicitudHttp(socketConexion);
            Thread hilo = new Thread(solicitud);
            hilo.start();
        }
    }
}
