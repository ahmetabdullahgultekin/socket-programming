```markdown
# Web Server Project

## Overview

This project implements a simple web server in Java using socket programming. The server can handle multiple client connections concurrently using a thread pool. It responds to HTTP requests with a basic HTML page.

## Features

- Handles multiple client connections concurrently.
- Responds to HTTP GET requests with a simple HTML page.
- Provides methods to start, stop, and restart the server.
- Displays server status and connection information.
- Returns an HTML document according to the requested URI size (between 100 and 20,000 bytes).
- Returns appropriate error messages for invalid requests.

## Requirements

- Java 11 or higher (Implemented by using JDK23)
- Maven

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── ahmetabdullahgultekin/
│   │           ├── Client.java
│   │           ├── Main.java
│   │           └── WebServer.java
│   └── resources/
└── test/


## Classes

### `WebServer`

This class represents the web server. It includes methods to create, start, stop, and restart the server. It uses a `ThreadPoolExecutor` to handle multiple client connections concurrently.

### `Client`

This class represents a client that can connect to the web server. It includes methods to create, start, and stop the client.

### `Main`

This class contains the `main` method to start the web server.

## Usage

### Running the Server

1. **Compile the project:**

   ```sh
   mvn clean install
   ```

2. **Run the server:**

   ```sh
   java -cp target/webserver-1.0-SNAPSHOT.jar com.ahmetabdullahgultekin.Main <port>
   ```

   Replace `<port>` with the desired port number (between 1024 and 65535).

### Example

To run the server on port 8080:

```sh
java -cp target/webserver-1.0-SNAPSHOT.jar com.ahmetabdullahgultekin.Main
```

## Code Explanation

### `WebServer.java`

The `WebServer` class handles the creation, starting, and stopping of the server. It uses a `ThreadPoolExecutor` to manage multiple client connections concurrently. The server responds to HTTP GET requests with a simple HTML page or an error message based on the request.

### `Main.java`

The `Main` class contains the `main` method, which is the entry point of the application. It validates the command-line arguments, creates an instance of the `WebServer`, and starts it.

### `Client.java`

The `Client` class represents a client that can connect to the web server. It includes methods to create, start, and stop the client.

## Error Handling

- If the requested URI size is less than 100 or greater than 20,000, the server returns a `400 Bad Request` error.
- If the requested URI is not a valid integer, the server returns a `400 Bad Request` error.
- If the HTTP method is not GET, the server returns a `501 Not Implemented` error for valid HTTP methods or a `400 Bad Request` error for invalid methods.

## Logging

The server logs information about every message received and every message sent, including errors and status updates.
```