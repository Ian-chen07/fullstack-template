package backend;

import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/api/login")
    public Map<String, Object> login(@RequestParam String username, @RequestParam String password) {
        return auth.login(username, password);
    }

    @PostMapping("/api/logout")
    public Map<String, Object> logout(
            @RequestHeader(value = "X-Token", defaultValue = "") String token) {
        auth.logout(token);
        return Map.of("ok", true);
    }
}
