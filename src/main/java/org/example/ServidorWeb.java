package org.example;

import java.io.*;
import java.net.*;

public final class ServidorWeb {
    public static void main(String argv[]) throws Exception {
        // Establece el número de puerto.
        int puerto = 6789;

        // Estableciendo el socket de escucha.
        ServerSocket socketEscucha = new ServerSocket(puerto);

        // Procesando las solicitudes HTTP en un ciclo infinito.
        while (true) {
            // Escuchando las solicitudes de conexión TCP.
            Socket socketConexion = socketEscucha.accept();

            // Construye un objeto para procesar la solicitud HTTP.
            SolicitudHttp solicitud = new SolicitudHttp(socketConexion);

            // Crea un nuevo hilo para procesar la solicitud.
            Thread hilo = new Thread(solicitud);

            // Inicia el hilo.
            hilo.start();
        }
    }
}

final class SolicitudHttp implements Runnable {
    final static String CRLF = "\r\n";
    Socket socket;

    // Constructor
    public SolicitudHttp(Socket socket) {
        this.socket = socket;
    }

    // Implementa el método run() de la interface Runnable.
    public void run() {
        try {
            proceseSolicitud();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void proceseSolicitud() throws Exception {
        // Referencia al stream de salida del socket.
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        // Referencia y filtros para el stream de entrada.
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Recoge la línea de solicitud HTTP del mensaje.
        String linea = in.readLine();
        // Muestra la línea de solicitud en la pantalla.
        System.out.println(linea);

        // Recoge y muestra las líneas de header.
        while ((linea = in.readLine()) != null && !linea.isEmpty()) {
            System.out.println(linea);
        }

        // Cierra los streams y el socket.
        out.close();
        in.close();
        socket.close();
    }
}
