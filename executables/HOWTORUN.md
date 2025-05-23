# Running the JAR File

This guide explains how to run the JAR file for the socket programming project.

## Prerequisites

- Java Development Kit (JDK) installed (version 8 or higher)
- Maven installed (for building the project)

## Running the web-server JAR File

1. Navigate to the directory in terminal (or open terminal in the directory of jar file) where you located to jar file:
    ```sh
    cd executables
    ```

2. Run the JAR file:
    ```sh
    java -jar proxy-server-1.0.jar
    ```

   Ensure that you use the correct name of the JAR file generated by Maven.

## Running the proxy-server JAR File

1. Navigate to the directory in terminal (or open terminal in the directory of jar file) where you located to jar file:
    ```sh
    cd executables
    ```

2. Run the JAR file:
    ```sh
    java -jar web-server-1.0.jar
    ```

   Ensure that you use the correct name of the JAR file generated by Maven.


## Additional Information

- The proxy server listens on port `8888` by default.
- You can modify the port and other configurations in the `ProxyServer` class.

For more details, refer to the project documentation or source code.