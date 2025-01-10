package com.gauntletai.chat.domain;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.gauntletai.chat.config.SecurityUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SseService {
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Map<String, Instant> userLastSeen = new ConcurrentHashMap<>();
    private static final Duration PRESENCE_TIMEOUT = Duration.ofMinutes(1);
    private final ScheduledExecutorService heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();
    private static final long HEARTBEAT_INTERVAL = 30; // seconds
    private static final long EMITTER_TIMEOUT = 3600000L; // 1 hour
    
    public SseService() {
        // Schedule heartbeat task
        heartbeatExecutor.scheduleAtFixedRate(this::sendHeartbeat, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.SECONDS);
    }
    
    public SseEmitter createEmitter(String chatId) {
        String userId = SecurityUtils.getCurrentUserId();
        String emitterId = chatId + "_" + userId;
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT);
        
        emitter.onCompletion(() -> removeEmitter(emitterId, userId));
        emitter.onTimeout(() -> removeEmitter(emitterId, userId));
        emitter.onError(e -> {
            log.error("Error for emitter: " + emitterId, e);
            removeEmitter(emitterId, userId);
        });
        
        emitters.put(emitterId, emitter);
        updatePresence();
        
        // Send initial connection event
        try {
            emitter.send(SseEmitter.event()
                .name("CONNECTED")
                .data("Connected successfully"));
        } catch (IOException e) {
            log.error("Failed to send initial connection event", e);
            removeEmitter(emitterId, userId);
            throw new RuntimeException("Failed to establish SSE connection", e);
        }
        
        return emitter;
    }
    
    private void sendHeartbeat() {
        emitters.forEach((emitterId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                    .name("HEARTBEAT")
                    .data(Instant.now().toString()));
            } catch (IOException e) {
                log.warn("Failed to send heartbeat to {}, removing emitter", emitterId);
                emitters.remove(emitterId);
            }
        });
    }
    
    public void updatePresence() {
        String userId = SecurityUtils.getCurrentUserId();
        userLastSeen.put(userId, Instant.now());
        broadcastPresenceUpdate();
    }
    
    private void removeEmitter(String emitterId, String userId) {
        emitters.remove(emitterId);
        // Only remove presence if user has no other emitters
        if (emitters.keySet().stream().noneMatch(id -> id.endsWith("_" + userId))) {
            userLastSeen.remove(userId);
            broadcastPresenceUpdate();
        }
    }
    
    public Set<String> getOnlineUsers() {
        Instant cutoff = Instant.now().minus(PRESENCE_TIMEOUT);
        return userLastSeen.entrySet().stream()
            .filter(entry -> entry.getValue().isAfter(cutoff))
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
    }
    
    public void broadcastToChat(String chatId, String eventName, Object data) {
        emitters.forEach((emitterId, emitter) -> {
            if (emitterId.startsWith(chatId + "_")) {
                try {
                    emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
                } catch (IOException e) {
                    log.error("Failed to send to emitter: " + emitterId, e);
                    String userId = emitterId.substring(emitterId.indexOf("_") + 1);
                    removeEmitter(emitterId, userId);
                }
            }
        });
    }
    
    private void broadcastPresenceUpdate() {
        Set<String> onlineUsers = getOnlineUsers();
        emitters.forEach((emitterId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                    .name("PRESENCE_UPDATE")
                    .data(onlineUsers));
            } catch (IOException e) {
                log.error("Failed to send presence update to: " + emitterId, e);
                emitters.remove(emitterId);
            }
        });
    }

    public SseEmitter subscribeToChat(String chatId) {
        String userId = SecurityUtils.getCurrentUserId();
        SseEmitter emitter = createEmitter(chatId);

        try {
            emitter.send(SseEmitter.event()
            .name("ONLINE_USERS")
            .data(getOnlineUsers()));
        } catch (IOException e) {
            log.error("Failed to send connected event to: " + userId, e);
        }

        return emitter;
    }
}