
package org.example.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class InstalledGamesStore {
    private static InstalledGamesStore instance;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Path storagePath;
    private Map<String, Map<String, String>> installedByUser = new HashMap<>();

    private InstalledGamesStore() {
        String home = System.getProperty("user.home");
        Path dir = Path.of(home, ".player_simulator");
        storagePath = dir.resolve("installed_games.json");
        try {
            if (!Files.exists(dir)) Files.createDirectories(dir);
            if (Files.exists(storagePath)) {
                byte[] bytes = Files.readAllBytes(storagePath);
                if (bytes.length > 0) {
                    installedByUser = mapper.readValue(bytes, new TypeReference<Map<String, Map<String, String>>>(){});
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            installedByUser = new HashMap<>();
        }
        if (installedByUser == null) installedByUser = new HashMap<>();
    }

    public static synchronized InstalledGamesStore getInstance() {
        if (instance == null) instance = new InstalledGamesStore();
        return instance;
    }

    public synchronized Set<String> getInstalledForUser(String userId) {
        if (userId == null) return Collections.emptySet();
        return new HashSet<>(installedByUser.getOrDefault(userId, Collections.emptyMap()).keySet());
    }

    public synchronized Map<String, String> getInstalledWithVersions(String userId) {
        if (userId == null) return Collections.emptyMap();
        return new HashMap<>(installedByUser.getOrDefault(userId, Collections.emptyMap()));
    }

    public synchronized boolean isInstalled(String userId, String gameId) {
        if (userId == null || gameId == null) return false;
        return installedByUser.getOrDefault(userId, Collections.emptyMap()).containsKey(gameId);
    }

    public synchronized void markInstalled(String userId, String gameId) {
        markInstalled(userId, gameId, null);
    }

    public synchronized void markInstalled(String userId, String gameId, String version) {
        if (userId == null || gameId == null) return;
        installedByUser.computeIfAbsent(userId, k -> new HashMap<>()).put(gameId, version);
        persist();
    }

    public synchronized void markUninstalled(String userId, String gameId) {
        if (userId == null || gameId == null) return;
        Map<String, String> map = installedByUser.get(userId);
        if (map != null) {
            map.remove(gameId);
            persist();
        }
    }

    private synchronized void persist() {
        try {
            File f = storagePath.toFile();
            mapper.writerWithDefaultPrettyPrinter().writeValue(f, installedByUser);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
