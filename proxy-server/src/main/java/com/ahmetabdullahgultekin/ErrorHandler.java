package com.ahmetabdullahgultekin;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ErrorHandler {

    // Constant URI Limit
    public static final short URI_LIMIT = 9999;
    private final InetSocketAddress webServerAddress;

    public ErrorHandler(InetSocketAddress webServerAddress) {
        this.webServerAddress = webServerAddress;
    }

    // get the request line from the request message
    public static Matcher getRequestLineMatcher(String request) {
        // Check first line of the request with regex
        Pattern pattern = Pattern.compile("GET /.* HTTP/.*");
        return pattern.matcher(request);
    }

    public boolean isNotFound() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(webServerAddress.getHostName(), webServerAddress.getPort()), 2000);
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    public boolean isRequestUriTooLong(int uriLength) {
        return uriLength > URI_LIMIT;
    }
}