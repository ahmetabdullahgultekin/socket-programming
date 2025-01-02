package com.ahmetabdullahgultekin;

import com.ahmetabdullahgultekin.enums.ResponseCode;
import com.ahmetabdullahgultekin.enums.State;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ConnectionHandler {

    private static final Logger LOGGER = LogManager.getLogger(ConnectionHandler.class);
    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    ConnectionHandler(ServerSocket serverSocket) {
        this.acceptConnections(serverSocket);
    }

    public static void terminate() {
        try {
            // Shutdown the executor service
            executorService.shutdown();
            // Log the termination
            LOGGER.info("Connections terminated");
        } catch (Exception e) {
            // Log the exception
            LOGGER.error("Failed to terminate the connections");
        }
    }

    private void acceptConnections(ServerSocket serverSocket) {
        try {
            while (true) {
                if (serverSocket.isClosed()) {
                    throw new IllegalStateException("Server socket is closed.");
                }
                if (WebServer.getState() == State.STOPPED) {
                    break;
                }
                // Log the number of active connections
                LOGGER.info("{} connection(s) active\n", ((ThreadPoolExecutor) executorService).getActiveCount());
                // Accept the connection
                Socket socket = serverSocket.accept();
                // Create a new thread and Handle the connection
                executorService.submit(() -> handleRequest(socket));
            }

        } catch (IOException e) {
            // Log the exception
            LOGGER.error("Failed to accept connection on port {}", serverSocket.getLocalPort());
        }
    }

    private void handleRequest(Socket client) {

        try (Socket clientSocket = client) {

            // Read the client's request
            String request = this.readRequest(clientSocket);

            // Decide if the request is valid
            ResponseCode responseCode = this.determineResponseCode(request);

            // Send HTML response to the client
            this.sendResponse(clientSocket, responseCode, request);

        } catch (Exception e) {
            LOGGER.error("Failed to handle the request on port {} ({})", client.getPort(), e.getMessage());
        }
    }


    private String readRequest(Socket clientSocket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            StringBuilder requestStr = new StringBuilder();
            // Read the client's request
            String line;
            //requestStr.append("Request from port ").append(clientSocket.getPort()).append("\n");
            while ((line = in.readLine()) != null) {
                if (line.isEmpty()) {
                    break;
                }
                requestStr.append(line).append("\n");
                //LOGGER.info(line);
            }
            LOGGER.info("{}{}", "Request from port " + clientSocket.getPort() + "\n", requestStr);

            return requestStr.toString();
        } catch (Exception e) {
            // Log the exception
            LOGGER.error("Failed to read the request");
            LOGGER.error(e.getMessage());
        }
        return null;
    }


    private void sendResponse(Socket clientSocket, ResponseCode responseCode, String request) {
        try {

            // Send HTML response to the client
            String message = switch (responseCode) {
                case OK -> "200 OK";
                case BAD_REQUEST -> "400 Bad Request";
                case NOT_IMPLEMENTED -> "501 Not Implemented";
            };
            short responseSize = -1;
            try {
                responseSize = Short.parseShort(request.split(" ")[1].substring(1));
            } catch (Exception e) {
                // Log the exception
                LOGGER.error("Failed to parse the response size {}", responseSize);
            }

            // Generate the HTML body based on the response size
            StringBuilder response = new StringBuilder();
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            if (responseCode == ResponseCode.OK) {
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

                // Start preparing the response
                // Create a new print writer
                out.println("HTTP/1.1 " + message);
                out.println("Content-Type: text/html");
                out.println("Content-Length: " + response.length());
                out.println("Connection: close");
                out.println();
                out.println(response);
            } else if (responseCode == ResponseCode.BAD_REQUEST) {
                out.println("HTTP/1.1 400 Bad Request");
            } else {
                out.println("HTTP/1.1 501 Not Implemented");
            }

            // Log the response
            LOGGER.info("Sent response to {}: HTTP/1.1 {} with Content-Length: {}",
                    clientSocket.getPort(), message, response.length());


        } catch (Exception e) {
            // Log the exception
            LOGGER.error("Failed to send the response on port {}", clientSocket.getPort());
            LOGGER.error(e.getMessage());
        }
    }

    private ResponseCode determineResponseCode(String request) {
        /*
        200 OK - The request is valid and the response is sent successfully. The client will receive the requested file.
        400 Bad Request - The request is invalid. The client will receive a 400 Bad Request HTML page.
        404 Not Found - The requested file is not found. The client will receive a 404 Not Found HTML page.
         */
        ResponseCode responseCode;
        // Check if the request is 400 Bad Request
        responseCode = ErrorHandler.isBadRequest(request);
        if (responseCode != ResponseCode.OK) {
            return responseCode;
        }
        // Check if the request is 501 Not Implemented
        responseCode = ErrorHandler.isNotImplemented(request);
        // Return 200 OK
        return responseCode;
    }
}
