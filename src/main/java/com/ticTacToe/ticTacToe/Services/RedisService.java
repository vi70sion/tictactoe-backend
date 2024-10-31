package com.ticTacToe.ticTacToe.Services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ticTacToe.ticTacToe.Models.GameSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import redis.clients.jedis.JedisPoolConfig;

@Service
public class RedisService {
    private final JedisPool jedisPool;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RedisService(@Value("${redis.host}") String host, @Value("${redis.port}") int port) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(5);
        this.jedisPool = new JedisPool(host, port);

    }

    public void put(String key, GameSession gameSession) {
        try (Jedis jedis = this.jedisPool.getResource()) {
            String json = objectMapper.writeValueAsString(gameSession);
            jedis.set(key, json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public GameSession getGameSession(String key) {
        try (Jedis jedis = this.jedisPool.getResource()) {
            String json = jedis.get(key);
            return objectMapper.readValue(json, GameSession.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Set<GameSession> getAllGameSessions() {
        Set<GameSession> gameSessions = new HashSet<>();
        try (Jedis jedis = this.jedisPool.getResource()) {
            Set<String> keys = jedis.keys("*");
            for (String key : keys) {
                String json = jedis.get(key);
                if (json != null) {
                    GameSession session = objectMapper.readValue(json, GameSession.class);
                    gameSessions.add(session);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return gameSessions;
    }

    public void close() {
        this.jedisPool.close();
    }
}
