package com.ahmetabdullahgultekin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLSession;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConnectionHandler {

    private final static Logger LOGGER = LogManager.getLogger(ConnectionHandler.class);
    private final ServerSocket serverSocket;
    private final InetSocketAddress webServerAddress;
    private final InetSocketAddress proxyAddress;
    private final ErrorHandler errorHandler;
    private final ExecutorService executorService;
    private URI requestUri;
    private static final Cache cache;

    static {
        try {
            cache = new Cache("cache");
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize cache", e);
        }
    }

    public ConnectionHandler(InetSocketAddress PROXY_ADDRESS, InetSocketAddress WEB_SERVER_ADDRESS, ServerSocket serverSocket) {
        this.proxyAddress = PROXY_ADDRESS;
        this.webServerAddress = WEB_SERVER_ADDRESS;
        this.serverSocket = serverSocket;
        this.errorHandler = new ErrorHandler(WEB_SERVER_ADDRESS);
        this.executorService = Executors.newCachedThreadPool();
        this.listen(serverSocket);
    }

    private void listen(ServerSocket proxySocket) {
        // Listen for incoming connections
        try {
            while (true) {
                // Accept connection
                Socket clientSocket = proxySocket.accept();
                // Handle connection
                LOGGER.info("Client connected: {}", clientSocket.getInetAddress());
                this.executorService.execute(() -> {
                    // Connect client
                    // Communicate
                    this.communicate(clientSocket);
                    // Disconnect client
                    this.disconnectClient(clientSocket);
                    // Disconnect server
                });

                if (proxySocket.isClosed()) {
                    LOGGER.warn("Proxy server is closed");
                    break;
                }
            }
        } catch (Exception e) {
            // Log error
            LOGGER.error("ResponseCode occurred while listening for incoming connections: {}", e.getMessage());
        }

    }

    private void communicate(Socket clientSocket) {
        /*
        * // Read the client's request
            String request = this.readRequest(clientSocket);
            assert request != null;
            String cachedResponse = cache.get(request);

            LOGGER.info("Cached response: {}", cachedResponse);

            if (cachedResponse != null) {
                sendCachedResponse(clientSocket, cachedResponse);
            } else {
                ResponseCode responseCode = this.determineResponseCode(request);
                String response = this.generateResponse(responseCode, request);
                cache.put(request, response);
                sendResponse(clientSocket, response);
            }*/
        // Communicate
        // Read request from client
        HttpRequest request = this.readRequest(clientSocket);
        assert request != null;
        String cachedResponse = cache.get(String.valueOf(request.uri().toString()));
        if (cachedResponse != null) {
            this.sendCachedResponse(clientSocket, cachedResponse);
            return;
        }

        /*
        // Check cache
        String cacheKey = null, cachedResponse = null;
        if (request != null) {
            cacheKey = request.uri().toString();
            cachedResponse = cache.get(cacheKey);
        }
        if (cachedResponse != null && !cachedResponse.isEmpty()) {
            LOGGER.info("Cache hit for URI: {}", cacheKey);
            this.forwardResponse(clientSocket, createCachedHttpResponse(cachedResponse));
            return;
        }

         */
        // Forward request to server
        if (clientSocket.isClosed()) {
            LOGGER.warn("Client socket is closed");
            return;
        }

        HttpResponse<String> response = this.getResponse(request);
        // Forward response to client
        if (response == null) {
            LOGGER.error("Response is null");
            return;
        }

        /*
        // Cache the response
        try {
            cache.put(cacheKey, response.body());
        } catch (IOException e) {
            LOGGER.error("ResponseCode occurred while caching response: {}", e.getMessage());
        }

         */
        this.forwardResponse(clientSocket, response);
    }

    private void sendCachedResponse(Socket clientSocket, String response) {
        try {
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            out.println(response);
            LOGGER.info("Sent cached response to {}", clientSocket.getPort());
        } catch (IOException e) {
            LOGGER.error("Failed to send cached response on port {}", clientSocket.getPort());
        }
    }

    private void sendResponse(Socket clientSocket, String response) {
        try {
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            out.println(response);
            LOGGER.info("Sent response to {}", clientSocket.getPort());
        } catch (IOException e) {
            LOGGER.error("Failed to send response on port {}", clientSocket.getPort());
        }
    }

    /*
    private String generateResponse(ResponseCode responseCode, String request) {
        StringBuilder response = new StringBuilder();
        String message = switch (responseCode) {
            case OK -> "200 OK";
            case BAD_REQUEST -> "400 Bad Request";
            case NOT_IMPLEMENTED -> "501 Not Implemented";
        };

        if (responseCode == ResponseCode.OK) {
            short responseSize = -1;
            try {
                responseSize = Short.parseShort(Arrays.stream(request.split(" "))
                        .filter(s -> s.startsWith("/"))
                        .findFirst()
                        .orElseThrow()
                        .substring(1));
            } catch (Exception e) {
                LOGGER.error("Failed to parse the response size {}, {}", responseSize, e.getMessage());
            }

            responseSize -= 100; // 87 bytes for the HTML tags and title
            if (responseSize < 0) {
                responseSize = 0;
            }
            response.append("""
                    <HTML>
                    <HEAD>
                    <TITLE>vatansever gltkn eren server</TITLE>
                    </HEAD>
                    <BODY>""");
            response.append("87 bytes tags");
            response.append("<br>12 bytes".repeat(responseSize / 12));
            response.append("X".repeat(responseSize % 12));
            response.append("""
                    </BODY>
                    </HTML>""");

            response.insert(0, "HTTP/1.1 " + message + "\r\n" +
                    "Content-Type: text/html\r\n" +
                    "Content-Length: " + response.length() + "\r\n" +
                    "Connection: close\r\n\r\n");
        } else if (responseCode == ResponseCode.BAD_REQUEST) {
            response.append("""
                    HTTP/1.1 400 Bad Request""");
        } else {
            response.append("""
                    HTTP/1.1 501 Not Implemented""");
        }

        return response.toString();
    }

     */

    private HttpResponse<String> createCachedHttpResponse(String body) {
        return new HttpResponse<>() {
            @Override
            public int statusCode() {
                return 200;
            }

            @Override
            public HttpRequest request() {
                return null;
            }

            @Override
            public Optional<HttpResponse<String>> previousResponse() {
                return Optional.empty();
            }

            @Override
            public HttpHeaders headers() {
                return HttpHeaders.of(Map.of("Content-Type", List.of("text/html")), (k, v) -> true);
            }

            @Override
            public String body() {
                return body;
            }

            @Override
            public Optional<SSLSession> sslSession() {
                return Optional.empty();
            }

            @Override
            public URI uri() {
                return null;
            }

            @Override
            public HttpClient.Version version() {
                return HttpClient.Version.HTTP_1_1;
            }
        };
    }

    private HttpRequest readRequest(Socket clientSocket) {
        // Behave like a server
        // Read request
        try {
            // Get request from client
            BufferedReader clientReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            StringBuilder request = new StringBuilder();
            String line;
            LOGGER.warn("Reading request...");
            while ((line = clientReader.readLine()) != null) {
                request.append(line).append("\n");
                LOGGER.info("Line: {}", line);
                if (line.isEmpty()) {
                    break;
                }
            }

            String requestString = request.toString();
            // Check if request uri is too long
            String uri = requestString.split(" ")[1];


            if (uri.contains("localhost")) {
                String[] strings = uri.split("/");
                int length = -1;

                try {
                    length = Integer.parseInt(strings[strings.length - 1]);
                } catch (NumberFormatException e) {
                    LOGGER.error("ResponseCode occurred while parsing request length: {}", e.getMessage());
                }

                this.checkError(clientSocket, uri, length);
            }

            //LOGGER.info("Request taken: {}", request);
            return this.reshapeRequest(requestString, clientSocket);
        } catch (IOException e) {
            // Log error
            LOGGER.error("ResponseCode occurred while reading request: {}", e.getMessage());
        }
        return null;
    }

    private void checkError(Socket clientSocket, String uri, int length) {
        if (this.errorHandler.isRequestUriTooLong(length)) {
            LOGGER.error("Request URI is too long");
            HttpResponse<String> response = new HttpResponse<>() {
                @Override
                public int statusCode() {
                    return 414;
                }

                @Override
                public HttpRequest request() {
                    return null;
                }

                @Override
                public Optional<HttpResponse<String>> previousResponse() {
                    return Optional.empty();
                }

                @Override
                public HttpHeaders headers() {
                    return null;
                }

                @Override
                public String body() {
                    return "Request URI is too long";
                }

                @Override
                public Optional<SSLSession> sslSession() {
                    return Optional.empty();
                }

                @Override
                public URI uri() {
                    return null;
                }

                @Override
                public HttpClient.Version version() {
                    return null;
                }
            };
            this.forwardResponse(clientSocket, response);
            disconnectClient(clientSocket);
        }

        // Check if web server is reachable
        if (this.errorHandler.isNotFound()) {
            LOGGER.error("Web server is not found");
            HttpResponse<String> response = new HttpResponse<>() {
                @Override
                public int statusCode() {
                    return 404;
                }

                @Override
                public HttpRequest request() {
                    return null;
                }

                @Override
                public Optional<HttpResponse<String>> previousResponse() {
                    return Optional.empty();
                }

                @Override
                public HttpHeaders headers() {
                    return null;
                }

                @Override
                public String body() {
                    return "Not Found";
                }

                @Override
                public Optional<SSLSession> sslSession() {
                    return Optional.empty();
                }

                @Override
                public URI uri() {
                    return null;
                }

                @Override
                public HttpClient.Version version() {
                    return null;
                }
            };
            this.forwardResponse(clientSocket, response);
            disconnectClient(clientSocket);
        }
    }

    private HttpResponse<String> getResponse(HttpRequest httpRequest) {
        // Behave like a client
        // Read response
        try (HttpClient httpClient = HttpClient.newHttpClient()) {
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            LOGGER.info("Response from web server: {}", response);
            return response;
        } catch (IOException e) {
            // Log error
            LOGGER.error("IO ResponseCode occurred while reading response: {}", e.getMessage());
            return possibleResponse(httpRequest);
        } catch (InterruptedException e) {
            LOGGER.error("Interrupted ResponseCode occurred while reading response: {}", e.getMessage());
            throw new RuntimeException(e);
        } catch (Exception e) {
            LOGGER.error("ResponseCode occurred while reading response: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private HttpResponse<String> possibleResponse(HttpRequest httpRequest) {
        return new HttpResponse<>() {
            @Override
            public int statusCode() {
                return 400;
            }

            @Override
            public HttpRequest request() {
                return httpRequest;
            }

            @Override
            public Optional<HttpResponse<String>> previousResponse() {
                return Optional.empty();
            }

            @Override
            public HttpHeaders headers() {
                return null;
            }

            @Override
            public String body() {
                return "";
            }

            @Override
            public Optional<SSLSession> sslSession() {
                return Optional.empty();
            }

            @Override
            public URI uri() {
                return null;
            }

            @Override
            public HttpClient.Version version() {
                return null;
            }
        };
    }

    private void forwardResponse(Socket clientSocket, HttpResponse<String> response) {
        // Behave like a regular server
        // Forward response
        try {
            // Send response to client
            if (response.statusCode() == 200) {
                PrintWriter clientWriter = new PrintWriter(clientSocket.getOutputStream(), true);
                clientWriter.println("HTTP/1.1 " + response.statusCode());
                clientWriter.println("Content-Type: text/html");
                clientWriter.println();
                clientWriter.println(response.body());
            } else {
                PrintWriter clientWriter = new PrintWriter(clientSocket.getOutputStream(), true);
                clientWriter.println("HTTP/1.1 " + response.statusCode());
                clientWriter.println("Content-Type: text/html");
                clientWriter.println();
            }

            LOGGER.info("Response forwarded: {}", response);
        } catch (IOException e) {
            // Log error
            LOGGER.error("ResponseCode occurred while forwarding response: {}", e.getMessage());
        }
    }


    private HttpRequest relayData(Socket clientSocket, URI uri) {
        // Relay data
        String host = uri.getHost();
        int port = uri.getPort();
        LOGGER.info("Relaying data to {}:{}", host, port);
        try (Socket webServerSocket = new Socket(host, port)) {

            InputStream clientInput = clientSocket.getInputStream();
            OutputStream clientOutput = clientSocket.getOutputStream();
            InputStream serverInput = webServerSocket.getInputStream();
            OutputStream serverOutput = webServerSocket.getOutputStream();

            Thread clientToServer = new Thread(() -> {
                try {
                    clientInput.transferTo(serverOutput);
                } catch (IOException e) {
                    LOGGER.error("ResponseCode relaying data from client to server: {}", e.getMessage());
                }
            });

            Thread serverToClient = new Thread(() -> {
                try {
                    serverInput.transferTo(clientOutput);
                } catch (IOException e) {
                    LOGGER.error("ResponseCode relaying data from server to client: {}", e.getMessage());
                }
            });

            clientToServer.start();
            serverToClient.start();

            clientToServer.join();
            serverToClient.join();

            disconnectClient(clientSocket);
        } catch (IOException | InterruptedException e) {
            LOGGER.error("ResponseCode occurred while relaying data: {}", e.getMessage());
        }
        return null;
    }

    private HttpRequest reshapeRequest(String request, Socket clientSocket) {
        // Reshape request
        String method;
        URI uri;
        try {
            String[] requestParts = request.split(" ");
            method = requestParts[0];
            String scheme = "https", host = requestParts[1];
            uri = URI.create(host);

            if (!uri.getScheme().equals("http") && !uri.getScheme().equals("https")) {
                uri = URI.create(scheme + "://" + host);
            }

        } catch (Exception e) {
            // Log error
            LOGGER.error("ResponseCode occurred while reshaping request: {}", e.getMessage());
            throw new RuntimeException(e);
        }


        return switch (method) {
            case "CONNECT" -> this.relayData(clientSocket, uri);
            case "GET" -> this.createGetRequest(uri);
            case "POST" -> this.createPostRequest(uri, request);
            case "PUT" -> this.createPutRequest(uri, request);
            case "DELETE" -> this.createDeleteRequest(uri);
            default -> null;
        };
    }

    private HttpRequest createGetRequest(URI uri) {
        return HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "text/html")
                .GET()
                .timeout(Duration.ofSeconds(10))
                .build();
    }

    private HttpRequest createPostRequest(URI uri, String request) {
        return HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "text/html")
                .POST(HttpRequest.BodyPublishers.ofString(request))
                .timeout(Duration.ofSeconds(10))
                .build();
    }

    private HttpRequest createPutRequest(URI uri, String request) {
        return HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "text/html")
                .PUT(HttpRequest.BodyPublishers.ofString(request))
                .timeout(Duration.ofSeconds(10))
                .build();
    }

    private HttpRequest createDeleteRequest(URI uri) {
        return HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "text/html")
                .DELETE()
                .timeout(Duration.ofSeconds(10))
                .build();
    }


    private void disconnectClient(Socket clientSocket) {
        // Disconnect client
        try {
            clientSocket.close();
        } catch (Exception e) {
            // Log error
            LOGGER.error("ResponseCode occurred while disconnecting client: {}", e.getMessage());
        }
    }

    private void disconnectServer(ServerSocket serverSocket) {
        // Disconnect server
        try {
            serverSocket.close();
        } catch (Exception e) {
            // Log error
            LOGGER.error("ResponseCode occurred while disconnecting server: {}", e.getMessage());
        }
    }

    public boolean isClientConnected(Socket clientSocket) {
        // Check if client is connected
        return clientSocket.isConnected();
    }

    public boolean isServerConnected(ServerSocket serverSocket) {
        // Check if server is connected
        return !serverSocket.isClosed();
    }
}
