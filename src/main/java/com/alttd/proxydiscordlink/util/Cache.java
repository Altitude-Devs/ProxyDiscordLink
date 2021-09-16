package com.alttd.proxydiscordlink.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Cache {

    private final Map<UUID, String> playerCacheCode;

    public Cache() {
        playerCacheCode = new HashMap<>();
    }

    public void cacheCode(UUID uuid, String code) {
        playerCacheCode.put(uuid, code);
    }

    public String getCode(UUID uuid)
    {
        return playerCacheCode.get(uuid);
    }

    public UUID getUUID(String code) {
        return playerCacheCode.entrySet().stream()
                .filter(entry -> entry.getValue().equals(code))
                .map(Map.Entry::getKey)
                .findFirst().orElse(null);
    }

    public void removeCachedPlayer(UUID uuid)
    {
        playerCacheCode.remove(uuid);
    }
}
