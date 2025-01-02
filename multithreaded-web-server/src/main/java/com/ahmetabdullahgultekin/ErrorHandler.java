package com.ahmetabdullahgultekin;

import com.ahmetabdullahgultekin.enums.ResponseCode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ErrorHandler {

    private static final Logger LOGGER = LogManager.getLogger(ErrorHandler.class);

    ErrorHandler() {
    }

    // Check method for the creating the web server
    public static void isValidPort(int port) {
        if (port < 1024 || port > 65535) {
            throw new IllegalArgumentException("Please provide a valid port number between 1024 and 65535.");
        }
    }

    // get the request line from the request message
    public static Matcher getRequestLineMatcher(String request) {
        // Check first line of the request with regex
        Pattern pattern = Pattern.compile("GET /.* HTTP/.*");
        return pattern.matcher(request);
    }

    static ResponseCode isBadRequest(String request) {
        // Check if the requested file is valid for HTTP GET format
        // Check first line of the request with regex
        Matcher matcher = getRequestLineMatcher(request);
        if (!matcher.find()) {
            LOGGER.error("Request is invalid. Bad Request.");
            return ResponseCode.BAD_REQUEST;
        }
        String requestLine = matcher.group();
        // Check if the requested file size is valid
        try {
            // Find the requested file size
            int requestSize = Integer.parseInt(requestLine.split(" ")[1].substring(1));
            short MAX_RESPONSE_SIZE = 20000;
            if (requestSize > MAX_RESPONSE_SIZE) {
                LOGGER.error("Requested file size is too large. Bad Request.");
                LOGGER.error("Requested file size: {}", requestSize);
                return ResponseCode.BAD_REQUEST;
            }
            short MIN_RESPONSE_SIZE = 100;
            if (requestSize < MIN_RESPONSE_SIZE) {
                LOGGER.error("Requested file size is too small. Bad Request.");
                LOGGER.error("Requested file size: {}", requestSize);
                return ResponseCode.BAD_REQUEST;
            }
        } catch (NumberFormatException e) {
            LOGGER.error("Requested file size format is invalid {}", e.getMessage());
            return ResponseCode.BAD_REQUEST;
        } catch (Exception e) {
            LOGGER.error("Requested file size is invalid. {}", e.getMessage());
            return ResponseCode.BAD_REQUEST;
        }
        return ResponseCode.OK;
    }

    static ResponseCode isNotImplemented(String request) {
        /*
        GET /100 HTTP/1.1 valid
        GET /20000 HTTP/2.0 valid
        POST /300 HTTP/1.1 invalid
         */
        Pattern pattern = Pattern.compile("POST|PUT|DELETE|PATCH|OPTIONS|HEAD|CONNECT|TRACE /.* HTTP/.*");
        Matcher matcher = pattern.matcher(request);
        // Check if the request message is not implemented and return 501 Not Implemented
        if (matcher.find()) {
            LOGGER.error("Request is not implemented.");
            return ResponseCode.NOT_IMPLEMENTED;
        }
        return ResponseCode.OK;
    }
}
