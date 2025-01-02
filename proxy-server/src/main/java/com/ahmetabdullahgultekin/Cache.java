package com.ahmetabdullahgultekin;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;

public class Cache {
    private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();
    private final Path cacheFile;

    public Cache(String cacheDir) throws IOException {
        Path cacheDirPath = Paths.get(cacheDir);
        if (!Files.exists(cacheDirPath)) {
            Files.createDirectories(cacheDirPath);
        }
        this.cacheFile = cacheDirPath.resolve("cache.json");
        loadCache();
    }

    public String get(String key) {
        // Substring the value until connection
        if (key.contains("Connection:")) {
            key = key.substring(0, key.indexOf("Connection:"));
        }
        return cache.get(key);
    }

    public void put(String key, String value) throws IOException {
        // Substring the value until connection
        if (key.contains("Connection:")) {
            key = key.substring(0, key.indexOf("Connection:"));
        }
        // Put the key-value pair into the cache
        cache.put(key, value);
        saveCache();
    }

    private void loadCache() throws IOException {
        if (Files.exists(cacheFile)) {
            try (BufferedReader reader = Files.newBufferedReader(cacheFile)) {
                Gson gson = new Gson();
                ConcurrentHashMap<String, String> loadedCache = gson.fromJson(reader, new TypeToken<ConcurrentHashMap<String, String>>() {
                }.getType());
                if (loadedCache != null) {
                    cache.putAll(loadedCache);
                }
            }
        }
    }

    private void saveCache() throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(cacheFile)) {
            Gson gson = new Gson();
            gson.toJson(cache, writer);
        }
    }
}