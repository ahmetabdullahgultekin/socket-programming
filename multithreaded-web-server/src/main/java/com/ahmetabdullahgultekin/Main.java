package com.ahmetabdullahgultekin;

public class Main {

    // Check if the command line port number is provided
    public static int checkCommandLineArguments(String[] args) {
        if (args.length < 1) {
            System.out.println("Please provide a port number.");
            System.exit(0);
        } else if (args.length > 1) {
            System.out.println("Please provide only one port number.");
            System.exit(0);
        } else {
            try {
                int port = Integer.parseInt(args[0]);
                ErrorHandler.isValidPort(port);
                return port;
            } catch (NumberFormatException e) {
                System.out.println("Please provide a valid port number between 1024 and 65535.");
                System.exit(0);
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
                System.exit(0);
            }
        }
        return -1;
    }

    public static void main(String[] args) {

        // Check if the command line port number is provided
        int port = checkCommandLineArguments(args);

        // Create a new WebServer object
        WebServer webServer = new WebServer(port);
        // Start the web server
        webServer.start();
    }
}