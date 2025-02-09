package org.example;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

final class SolicitudHttp implements Runnable {
    private static final String CRLF = "\r\n";
    private final Socket socket;

    public SolicitudHttp(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            proceseSolicitud();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void proceseSolicitud() throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());

        String linea = in.readLine();
        if (linea == null) return;
        System.out.println(linea);

        StringTokenizer partesLinea = new StringTokenizer(linea);
        String metodo = partesLinea.nextToken(); // GET
        String nombreArchivo = partesLinea.nextToken(); // /index.html

        if (nombreArchivo.equals("/")) {
            nombreArchivo = "index.html";
        } else {
            nombreArchivo = nombreArchivo.substring(1);
        }

        while ((linea = in.readLine()) != null && !linea.isEmpty()) {
            System.out.println(linea);
        }

        File file = null;
        InputStream recurso = null;
        try {
            file = new File(ClassLoader.getSystemResource(nombreArchivo).toURI());
            if (file.exists() && file.isFile()) {
                recurso = new FileInputStream(file);
            }
        } catch (Exception e) {
            System.out.println("Archivo no encontrado: " + nombreArchivo);
        }

        String lineaDeEstado;
        String lineaHeader;

        if (recurso != null) {
            lineaDeEstado = "HTTP/1.0 200 OK" + CRLF;
            lineaHeader = "Content-Type: " + contentType(nombreArchivo) + CRLF +
                    "Content-Length: " + file.length() + CRLF + CRLF;

            enviarString(lineaDeEstado, out);
            enviarString(lineaHeader, out);
            enviarBytes(recurso, out);
            recurso.close();
        } else {
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

            enviarString(lineaDeEstado, out);
            enviarString(lineaHeader, out);
            enviarString(cuerpoMensaje, out);
        }

        out.flush();
        out.close();
        in.close();
        socket.close();
    }

    private static void enviarString(String line, OutputStream os) throws Exception {
        os.write(line.getBytes(StandardCharsets.UTF_8));
    }

    private static void enviarBytes(InputStream is, OutputStream os) throws Exception {
        byte[] buffer = new byte[1024];
        int bytes;
        while ((bytes = is.read(buffer)) != -1) {
            os.write(buffer, 0, bytes);
        }
    }

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
        return "application/octet-stream";
    }
}
