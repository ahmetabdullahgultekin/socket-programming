package com.ahmetabdullahgultekin;

import com.ahmetabdullahgultekin.enums.State;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;

public class WebServer {

    //private Set<String> uniqueClients;
    private static final Logger LOGGER = LogManager.getLogger(WebServer.class);
    // Instance variables
    private static int serverPort;
    private static ServerSocket serverSocket;
    private static State state = State.UNSET;

    // Unable to create a web server without a port number
    public WebServer() {
        throw new IllegalArgumentException("Port number is required.");
    }

    // Create a web server with a port number
    public WebServer(int serverPort) {
        this.printStartupBanner();
        create(serverPort);
    }

    // Create the web server
    public static void create(int port) {
        // Set the port number
        serverPort = port;
        // Change the status to created
        LOGGER.info("Server created on port {}", serverPort);
        state = State.CREATED;
    }

    public static State getState() {
        return state;
    }

    // Start the web server
    public void start() {
        // Run the web server on the port
        try {
            // Create a new server socket
            serverSocket = new ServerSocket(serverPort);
            // Change the status of the web server
            state = State.STARTED;
            LOGGER.info("Server started on port {}", serverPort);

            // Accept the connection
            new ConnectionHandler(serverSocket);

        } catch (IOException e) {
            // Log the exception
            LOGGER.error("Failed to start the server on port {}", serverPort);
            LOGGER.error(e.getMessage());
        } catch (Exception e) {
            // Log the exception
            LOGGER.error("Server failed on port {}", serverPort);
            LOGGER.error(e.getMessage());
        } finally {
            // Stop the web server
            this.stop();
        }
    }

    // Stop the web server
    private void stop() {
        // Stop the web server
        // Change the status of the web server
        LOGGER.info("Server stopping on port {}", serverPort);
        // Stop the executor services
        ConnectionHandler.terminate();
        // Close the server socket
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            // Log the exception
            LOGGER.error("Failed to close the server socket");
        }
        state = State.STOPPED;
        LOGGER.info("Server stopped on port {}", serverPort);
        this.printShutdown();
    }

    private void printStartupBanner() {
        System.out.print("""
                                 ██╗ ██╗                                          ███████╗██████╗ ███████╗███╗   ██╗                         ▄▄
                                ██╔╝██╔╝                                          ██╔════╝██╔══██╗██╔════╝████╗  ██║                       ▄▀  █
                               ██╔╝██╔╝                                           █████╗  ██████╔╝█████╗  ██╔██╗ ██║                     ▄▀█   █
                              ██╔╝██╔╝                                            ██╔══╝  ██╔══██╗██╔══╝  ██║╚██╗██║                  ▄▀▀▒█▒   █
                             ██╔╝██╔╝                                             ███████╗██║  ██║███████╗██║ ╚████║       ▄▀▀▄   ▄▄▀▀▒▒▒▒█▒▒  █
                             ╚═╝ ╚═╝                                              ╚══════╝╚═╝  ╚═╝╚══════╝╚═╝  ╚═══╝      █▒   ▀▄▀▒▒▒▒▒▒▒▒▒▒▒▒▒█
                           ██╗ ██╗                      ██████╗ ██╗   ██╗██╗  ████████╗███████╗██╗  ██╗██╗███╗   ██╗      █▒    ▒▀▄▒▒▒▒▒▒▒▒▒▒▒▒▒▀▄
                          ██╔╝██╔╝                     ██╔════╝ ██║   ██║██║  ╚══██╔══╝██╔════╝██║ ██╔╝██║████╗  ██║      █▒     ▒▒▒▒▒▒▒▒▒█▒█▒▒▒▒▒▀▄
                         ██╔╝██╔╝                      ██║  ███╗██║   ██║██║     ██║   █████╗  █████╔╝ ██║██╔██╗ ██║      █▀▄  ▒▒▒▒▒▒▒▒█▒▒▒█▒█▒▄▄▒▒█
                        ██╔╝██╔╝                       ██║   ██║██║   ██║██║     ██║   ██╔══╝  ██╔═██╗ ██║██║╚██╗██║     ██▒▒▀▒▒▒▒▒▒▒▒▒▒█▒▒▒▒▒█▄██▒▒█
                       ██╔╝██╔╝                        ╚██████╔╝╚██████╔╝███████╗██║   ███████╗██║  ██╗██║██║ ╚████║   ▄▀▒█▒▒▒▒▒▒▒▒▒▒▒▄▀██▒▒▒▒▒▀▀▒▒█   ▄
                       ╚═╝ ╚═╝                          ╚═════╝  ╚═════╝ ╚══════╝╚═╝   ╚══════╝╚═╝  ╚═╝╚═╝╚═╝  ╚═══╝  ▀▒▒▒▒█▒▒▒▒▒▒▒▄▒█████▄▒▒▒▒▒▒▒▄▀▀▀▀
                    ██╗ ██╗   ██╗   ██╗ █████╗ ████████╗ █████╗ ███╗   ██╗███████╗███████╗██╗   ██╗███████╗██████╗    ▒▒▒▒▒█▒▒▒▒▒▄▀▒▒▒▀▀▀▒▒▒▒▄█▀  ▒█▀▀▄▄
                   ██╔╝██╔╝   ██║   ██║██╔══██╗╚══██╔══╝██╔══██╗████╗  ██║██╔════╝██╔════╝██║   ██║██╔════╝██╔══██╗   ▒▒▒▒▒▒█▒▄▄▀▒▒▒▒▒▒▒▒▒▒▒  █▒▀▄▀▄    ▀
                  ██╔╝██╔╝    ██║   ██║███████║   ██║   ███████║██╔██╗ ██║███████╗█████╗  ██║   ██║█████╗  ██████╔╝   ▒▒▒▒▒▒▒█▒▒▒▒▒▒▒▒▒▄▒▒▒▒▄▀▒▒▒█  ▀▄
                 ██╔╝██╔╝     ╚██╗ ██╔╝██╔══██║   ██║   ██╔══██║██║╚██╗██║╚════██║██╔══╝  ╚██╗ ██╔╝██╔══╝  ██╔══██╗   ▒▒▒▒▒▒▒▒▀▄▒▒▒▒▒▒▒▒▀▀▀▀▒▒▒▄▀
                ██╔╝██╔╝       ╚████╔╝ ██║  ██║   ██║   ██║  ██║██║ ╚████║███████║███████╗ ╚████╔╝ ███████╗██║  ██║
                ╚═╝ ╚═╝         ╚═══╝  ╚═╝  ╚═╝   ╚═╝   ╚═╝  ╚═╝╚═╝  ╚═══╝╚══════╝╚══════╝  ╚═══╝  ╚══════╝╚═╝  ╚═╝
                """);

        LOGGER.info(" :: HTTP Server :: Gultekin, Eren & Vatansever :: v1.0 ::");

        LOGGER.info("Starting HTTPServer on localhost with PID {}", ProcessHandle.current().pid());
        LOGGER.info("Application is powered by the genius of Gultekin, Eren and Vatansever");
    }

    private void printShutdown() {
        LOGGER.info("Stopping HTTPServer on localhost with PID {}", ProcessHandle.current().pid());
    }
}
