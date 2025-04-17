# Socket Programming Suite

> **Java 17 · Maven · Log4j 2**

This repository showcases two core networking applications implemented from scratch in Java:

| Module | Purpose | Default port |
|--------|---------|--------------|
| **multithreaded‑web‑server** | Minimal HTTP/1.1 server with a thread‑pool, capable of serving static files and returning proper status codes | **8051** |
| **proxy‑server** | Forward HTTP proxy with in‑memory + JSON‑disk cache and basic content filtering | **8888** |

Executables are pre‑built in **`/executables`** for quick testing, while full Maven projects are provided for hacking on the source code.

---

## ✨ Features

### Multithreaded Web Server
* Accepts **GET** and **HEAD** requests
* Serves files from configurable `www/` root
* Returns **200/404/405/500** via `ResponseCode` enum
* **Cached thread pool** keeps the server responsive under load
* Per‑request and error logging via Log4j 2
* Graceful shutdown on `SIGINT`

### HTTP Proxy Server
* Listens on `127.0.0.1:8888` and forwards outgoing requests
* Transparent connection to the bundled web‑server (`localhost:8051`)
* **Concurrent client handling** with the same thread‑pool pattern
* Simple **LRU cache** implemented in `Cache.java`
* Persists cache to `cache/cache.json` between runs (Gson)

---

## 🗂️ Repository Layout

```text
socket-programming/
├── multithreaded-web-server/
│   ├── src/main/java/…         # server sources
│   └── pom.xml                 # Maven build
├── proxy-server/
│   ├── src/main/java/…         # proxy sources
│   └── pom.xml
├── executables/
│   ├── web-server-1.0.jar
│   └── proxy-server-1.0.jar
└── README.md                   # you are here
```

---

## 🚀 Quick Start

### 0. Prerequisites
* **JDK 17+**
* **Maven 3.9+**

### 1. Build from source
```bash
# Web server
cd multithreaded-web-server
mvn package        # target/web-server-1.0.jar

# Proxy
cd ../proxy-server
mvn package        # target/proxy-server-1.0.jar
```

### 2. Run the web server
```bash
java -jar target/web-server-1.0.jar 8051   # custom port optional
```
Browse `http://localhost:8051/index.html`

### 3. Run the proxy
```bash
java -jar target/proxy-server-1.0.jar
```
Set your browser or `curl` to use `127.0.0.1:8888` as HTTP proxy:
```bash
curl -x http://127.0.0.1:8888 http://localhost:8051/
```

### 4. Using the pre‑built jars
```bash
cd executables
java -jar web-server-1.0.jar
java -jar proxy-server-1.0.jar
```

---

## 📝 Configuration

Both servers read a small set of **constants** hard‑coded in their *Main* classes:

| Server | File | Key constants |
|--------|------|---------------|
| Web | `WebServer.java` | `ROOT_DIR`, `MAX_THREADS` |
| Proxy | `ProxyServer.java` | `PROXY_PORT`, `WEB_SERVER_PORT`, `CACHE_DIR` |

Edit & re‑build or refactor to external config if needed.

---

## 📑 Logs

Runtime logs are written to:
```
executables/logs/YYYY-MM-DD/server-log-<hh>-<n>.log
multithreaded-web-server/logs/…
proxy-server/logs/…
```
Use `tail -f` for live monitoring.

---

## 🧪 Testing

Unit tests live under `src/test/java` and can be run with:
```bash
mvn test
```
JUnit 4 and Mockito are used for behaviour verification.

---

## 🛠️ Extending Ideas

* Add **HTTPS** support with `SSLSocket`
* Implement **HTTP/1.1 keep‑alive** for persistent connections
* Enhance the proxy with **blacklist / whitelist** filtering
* Expose metrics via **JMX** or Micrometer

---

> Built for educational purposes — dive in, experiment, and learn!

