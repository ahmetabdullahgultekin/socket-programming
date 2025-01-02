package com.ahmetabdullahgultekin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.net.ServerSocket;

public class ProxyServer {

    // Constant proxy port number
    private final static short PROXY_PORT = 8888;
    private final static String PROXY_IP = "127.0.0.1";
    private final static String HOST = "localhost";
    private final static InetSocketAddress PROXY_ADDRESS = new InetSocketAddress(PROXY_IP, PROXY_PORT);
    // Constant web server port number
    private final static short WEB_SERVER_PORT = 8051;
    private final static InetSocketAddress WEB_SERVER_ADDRESS = new InetSocketAddress(HOST, WEB_SERVER_PORT);
    // Logger
    private static final Logger LOGGER = LogManager.getLogger(ProxyServer.class);
    // Server socket
    private static ServerSocket serverSocket;
    // Connection handler
    private static ConnectionHandler connectionHandler;

    public ProxyServer() {
        this.create();
    }

    private void create() {
        this.printBanner();
        LOGGER.info("Creating proxy server...");
        try {
            serverSocket = new ServerSocket(PROXY_PORT);
            //connectionHandler = new ConnectionHandler(PROXY_ADDRESS, WEB_SERVER_ADDRESS, serverSocket);
            LOGGER.info("Proxy server is created on port " + PROXY_PORT);
            this.start();
        } catch (Exception e) {
            LOGGER.error("ResponseCode occurred while creating proxy server: {}", e.getMessage());
        }
    }

    private void start() {
        LOGGER.info("Proxy server is starting...");
        try {
            LOGGER.info("Proxy server is started on port " + PROXY_PORT);
            new ConnectionHandler(PROXY_ADDRESS, WEB_SERVER_ADDRESS, serverSocket);
            //connectionHandler.listen(serverSocket);
        } catch (Exception e) {
            LOGGER.error("ResponseCode occurred while starting proxy server: {}", e.getMessage());
        }
    }

    private void terminate() {
        LOGGER.info("Proxy server is terminating...");
        try {
            serverSocket.close();
            LOGGER.info("Proxy server is terminated on port " + PROXY_PORT);
        } catch (Exception e) {
            LOGGER.error("ResponseCode occurred while terminating proxy server: {}", e.getMessage());
        }
        LOGGER.info("Proxy server is terminated on port " + PROXY_PORT);
    }

    private void printBanner() {
        System.out.println("""
                 _______  _______  _______                  \s
                (  ____ )(  ____ )(  ___  )|\\     /||\\     /|
                | (    )|| (    )|| (   ) |( \\   / )( \\   / )
                | (____)|| (____)|| |   | | \\ (_) /  \\ (_) /\s
                |  _____)|     __)| |   | |  ) _ (    \\   / \s
                | (      | (\\ (   | |   | | / ( ) \\    ) (  \s
                | )      | ) \\ \\__| (___) |( /   \\ )   | |  \s
                |/       |/   \\__/(_______)|/     \\|   \\_/  \s
                                                            \s
                """);
    }
}
