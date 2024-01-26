package org.proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Proxy_Server {

    // Este método es el punto de entrada para el programa. Inicia el servidor proxy en el puerto local especificado y establece la conexión con el servidor proxy remoto en el puerto y host proporcionados.
    public static void main(String[] args) {
        int Local_Port = 8080;
        System.out.println("Starting proxy on port " + Local_Port);

        try {
            Run_Server(Local_Port);
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    // Este método crea un bucle infinito que acepta conexiones entrantes de clientes en el puerto local. Por cada conexión entrante, crea un nuevo hilo para manejarla llamando al método handleConnection.
    public static void Run_Server(int Local_Port) throws IOException {
        try (ServerSocket Server_Socket = new ServerSocket(Local_Port)) {
            ExecutorService executorService = Executors.newCachedThreadPool();
            while (true) {
                try {
                    Socket Socket_Client = Server_Socket.accept();
                    executorService.submit(() -> handleConnection(Socket_Client));
                } catch (IOException e) {
                    System.err.println(e);
                }
            }
        }
    }

    // Este método se encarga de manejar la conexión entrante del cliente. Lee la primera línea de la solicitud del cliente para determinar si se trata de una solicitud HTTP regular o una solicitud HTTPS (CONNECT). En función de esto, llama a handleHttpRequest o handleHttpsRequest para manejar la solicitud correspondiente.
    private static void handleConnection(Socket Socket_Client) {
        String clientAddress = Socket_Client.getInetAddress().getHostAddress();
        System.out.println("Connected client: " + clientAddress);

        try (InputStream InputStreamClient = Socket_Client.getInputStream();
             OutputStream OutputStreamClient = Socket_Client.getOutputStream()) {

            // Read client request
            BufferedReader reader = new BufferedReader(new InputStreamReader(InputStreamClient));
            String requestLine = reader.readLine();
            if (requestLine == null) {
                System.err.println("Invalid request from client: " + clientAddress);
                return;
            }
            System.out.println("Request from " + clientAddress + ": " + requestLine);

            // Parse the request
            String[] requestParts = requestLine.split(" ");
            if (requestParts.length != 3) {
                System.err.println("Invalid request from client: " + clientAddress);
                return;
            }

            if (requestParts[0].equalsIgnoreCase("CONNECT")) {
                handleHttpsRequest(requestParts[1], Socket_Client, OutputStreamClient);
            } else {
                handleHttpRequest(requestParts, Socket_Client, OutputStreamClient);
            }
        } catch (IOException e) {
            System.err.println("Error handling connection: " + e);
        } finally {
            try {
                Socket_Client.close();
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e);
            }
        }
    }

    // Este método se encarga de manejar las solicitudes HTTP regulares. Lee y reenvía la solicitud del cliente al servidor de destino. Luego, lee y reenvía la respuesta del servidor de destino al cliente.
    private static void handleHttpRequest(String[] requestParts, Socket Socket_Client, OutputStream OutputStreamClient) {
        // Get the target host and port from the request
        URL targetUrl;
        try {
            targetUrl = new URL(requestParts[1]);
        } catch (MalformedURLException e) {
            System.err.println("Invalid URL in request: " + e);
            return;
        }
        String targetHost = targetUrl.getHost();
        int targetPort = targetUrl.getPort() != -1 ? targetUrl.getPort() : 80;

        try (Socket Socket_Server = new Socket(targetHost, targetPort); // Este es el punto exacto donde se oculta la dirección IP del cliente en peticiones HTTP.
             InputStream InputStreamServer = Socket_Server.getInputStream();
             OutputStream OutputStreamServer = Socket_Server.getOutputStream()) {

            // Forward the client request to the target server
            OutputStreamServer.write((String.join(" ", requestParts) + "\r\n").getBytes());
            BufferedReader clientReader = new BufferedReader(new InputStreamReader(Socket_Client.getInputStream()));
            while (true) {
                String headerLine = clientReader.readLine();
                if (headerLine == null || headerLine.trim().isEmpty()) {
                    break;
                }
                OutputStreamServer.write((headerLine + "\r\n").getBytes());
            }
            OutputStreamServer.write("\r\n".getBytes());

            // Read the target server's response and forward it to the client
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = InputStreamServer.read(buffer)) != -1) {
                OutputStreamClient.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            System.err.println("Error forwarding request: " + e);
        }
    }

    // Este método se encarga de manejar las solicitudes HTTPS (CONNECT). Establece una conexión con el servidor de destino y envía una respuesta de éxito (HTTP 200) al cliente. A continuación, crea dos hilos para reenviar datos entre el cliente y el servidor de destino, utilizando el método forwardData.
    private static void handleHttpsRequest(String targetHostPort, Socket Socket_Client, OutputStream OutputStreamClient) {
        String[] hostPortParts = targetHostPort.split(":");
        if (hostPortParts.length != 2) {
            System.err.println("Invalid CONNECT request format");
            return;
        }

        String targetHost = hostPortParts[0];
        int targetPort;
        try {
            targetPort = Integer.parseInt(hostPortParts[1]);
        } catch (NumberFormatException e) {
            System.err.println("Invalid target port in CONNECT request: " + e);
            return;
        }

        try (Socket Socket_Server = new Socket(targetHost, targetPort)) { // Este es el punto exacto donde se oculta la dirección IP del cliente en peticiones HTTPS.
            // Send a successful response to the client
            OutputStreamClient.write("HTTP/1.1 200 Connection Established\r\n\r\n".getBytes());

            // Forward data between the client and the target server
            ExecutorService executorService = Executors.newCachedThreadPool();
            executorService.submit(() -> forwardData(Socket_Client, Socket_Server));
            forwardData(Socket_Server, Socket_Client);
        } catch (IOException e) {
            System.err.println("Error handling CONNECT request: " + e);
        }
    }

    // Este método se encarga de leer datos del inputSocket y escribirlos en el outputSocket. Utiliza un búfer para leer y escribir datos en bloques. Si se produce un error de lectura o escritura, registra el error solo si ambas conexiones de socket aún están abiertas, ya que el error podría ser causado por el cierre normal de la conexión.
    private static void forwardData(Socket inputSocket, Socket outputSocket) {
        try (InputStream inputStream = inputSocket.getInputStream();
             OutputStream outputStream = outputSocket.getOutputStream()) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            if (!inputSocket.isClosed() && !outputSocket.isClosed()) {
                System.err.println("Error forwarding data: " + e);
            }
        }
    }
}


// El proxy oculta la dirección IP del cliente al actuar como intermediario entre el cliente y el servidor de destino. La dirección IP del cliente no se revela directamente al servidor de destino porque todas las solicitudes se envían a través del proxy.

// En el ejemplo anterior, esta ocultación ocurre en dos puntos, uno para solicitudes HTTP y otro para solicitudes HTTPS:

// Solicitudes HTTP: En el método handleHttpRequest, el proxy lee la solicitud del cliente y la envía al servidor de destino utilizando la conexión Socket_Server. La dirección IP del servidor de destino verá el proxy como el cliente en lugar de la dirección IP real del cliente. Esto ocurre aquí:
//     try (Socket Socket_Server = new Socket(targetHost, targetPort);
//     InputStream InputStreamServer = Socket_Server.getInputStream();
//     OutputStream OutputStreamServer = Socket_Server.getOutputStream()) {

// Solicitudes HTTPS: En el método handleHttpsRequest, el proxy establece una conexión con el servidor de destino utilizando la conexión Socket_Server. Al igual que en el caso de las solicitudes HTTP, la dirección IP del servidor de destino verá el proxy como el cliente en lugar de la dirección IP real del cliente. La ocultación de la dirección IP se realiza al establecer la conexión con el servidor de destino en las siguientes líneas:
//     try (Socket Socket_Server = new Socket(targetHost, targetPort)) {