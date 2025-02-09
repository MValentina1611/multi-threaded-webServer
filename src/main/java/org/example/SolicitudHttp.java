package org.example;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

// Handles each HTTP request in a separate thread
final class SolicitudHttp implements Runnable {
    private static final String CRLF = "\r\n"; // Carriage return and line feed for HTTP headers
    private final Socket socket;

    // Constructor to initialize the socket
    public SolicitudHttp(Socket socket) {
        this.socket = socket;
    }

    // Entry point for the thread
    public void run() {
        try {
            proceseSolicitud(); // Process the HTTP request
        } catch (Exception e) {
            System.out.println(e); // Log any exceptions
        }
    }

    // Method to handle the HTTP request and response
    private void proceseSolicitud() throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Read client input
        BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream()); // Send output to client

        String linea = in.readLine(); // Read the request line
        if (linea == null) return;
        System.out.println(linea); // Log the request line

        StringTokenizer partesLinea = new StringTokenizer(linea);
        String metodo = partesLinea.nextToken(); // Extract HTTP method (e.g., GET)
        String nombreArchivo = partesLinea.nextToken(); // Extract requested file name

        // Default to index.html if root is requested
        if (nombreArchivo.equals("/")) {
            nombreArchivo = "index.html";
        } else {
            nombreArchivo = nombreArchivo.substring(1); // Remove leading slash
        }

        // Read and log HTTP request headers
        while ((linea = in.readLine()) != null && !linea.isEmpty()) {
            System.out.println(linea);
        }

        File file = null;
        InputStream recurso = null;
        try {
            // Locate the requested file in the resources folder
            file = new File(ClassLoader.getSystemResource(nombreArchivo).toURI());
            if (file.exists() && file.isFile()) {
                recurso = new FileInputStream(file); // Open file for reading
            }
        } catch (Exception e) {
            System.out.println("Archivo no encontrado: " + nombreArchivo); // Log file not found
        }

        String lineaDeEstado;
        String lineaHeader;

        if (recurso != null) {
            // If the file exists, send a 200 OK response
            lineaDeEstado = "HTTP/1.0 200 OK" + CRLF;
            lineaHeader = "Content-Type: " + contentType(nombreArchivo) + CRLF +
                    "Content-Length: " + file.length() + CRLF + CRLF;

            enviarString(lineaDeEstado, out); // Send HTTP status line
            enviarString(lineaHeader, out);   // Send HTTP headers
            enviarBytes(recurso, out);        // Send file content
            recurso.close();
        } else {
            // If the file does not exist, send a 404 Not Found response
            InputStream recurso404 = ClassLoader.getSystemResourceAsStream("404.html");
            String cuerpoMensaje;
            if (recurso404 != null) {
                cuerpoMensaje = new String(recurso404.readAllBytes(), StandardCharsets.UTF_8);
                recurso404.close();
            } else {
                cuerpoMensaje = "<html><body><h1>404 - Recurso no encontrado</h1></body></html>";
            }

            lineaDeEstado = "HTTP/1.0 404 Not Found" + CRLF;
            lineaHeader = "Content-Type: text/html" + CRLF +
                    "Content-Length: " + cuerpoMensaje.length() + CRLF + CRLF;

            enviarString(lineaDeEstado, out); // Send 404 status line
            enviarString(lineaHeader, out);   // Send headers
            enviarString(cuerpoMensaje, out); // Send error message body
        }

        // Clean up and close streams and socket
        out.flush();
        out.close();
        in.close();
        socket.close();
    }

    // Helper method to send a string over the output stream
    private static void enviarString(String line, OutputStream os) throws Exception {
        os.write(line.getBytes(StandardCharsets.UTF_8));
    }

    // Helper method to send bytes from an input stream to the output stream
    private static void enviarBytes(InputStream is, OutputStream os) throws Exception {
        byte[] buffer = new byte[1024];
        int bytes;
        while ((bytes = is.read(buffer)) != -1) {
            os.write(buffer, 0, bytes);
        }
    }

    // Determine the content type based on the file extension
    private static String contentType(String nombreArchivo) {
        if (nombreArchivo.endsWith(".htm") || nombreArchivo.endsWith(".html")) {
            return "text/html";
        }
        if (nombreArchivo.endsWith(".jpg") || nombreArchivo.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (nombreArchivo.endsWith(".gif")) {
            return "image/gif";
        }
        if (nombreArchivo.endsWith(".png")) {
            return "image/png";
        }
        return "application/octet-stream"; // Default binary content type
    }
}