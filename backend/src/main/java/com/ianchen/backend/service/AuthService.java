package com.ianchen.backend.service;

import com.ianchen.backend.entity.User;
import com.ianchen.backend.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository users;
    private final Map<String, Long> tokens = new ConcurrentHashMap<>();

    public AuthService(UserRepository users) {
        this.users = users;
    }

    public Map<String, Object> login(String username, String password) {
        User user = users.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户名或密码错误"));
        if (!user.password.equals(password)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
        }

        String token = UUID.randomUUID().toString();
        tokens.put(token, user.id);

        Map<String, Object> body = new HashMap<>();
        body.put("token", token);
        body.put("username", user.username);
        body.put("role", user.role);
        return body;
    }

    public void logout(String token) {
        if (token != null) {
            tokens.remove(token);
        }
    }

    public User require(String token) {
        Long id = token == null ? null : tokens.get(token);
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "请先登录");
        }
        return users.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "请先登录"));
    }
}
