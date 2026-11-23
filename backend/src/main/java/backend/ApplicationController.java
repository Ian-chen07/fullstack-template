package backend;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApplicationController {

    private final ApplicationRepository repository;
    private final BudgetService service;
    private final AuthService auth;

    public ApplicationController(ApplicationRepository repository, BudgetService service, AuthService auth) {
        this.repository = repository;
        this.service = service;
        this.auth = auth;
    }

    @GetMapping("/api/applications")
    public List<Application> list() {
        return repository.findAll();
    }

    @PostMapping("/api/applications")
    public Application create(
            @RequestHeader(value = "X-Token", defaultValue = "") String token,
            @RequestParam String purpose,
            @RequestParam BigDecimal amount) {
        return service.create(auth.require(token), purpose, amount);
    }

    @PostMapping("/api/applications/{id}/submit")
    public Application submit(
            @RequestHeader(value = "X-Token", defaultValue = "") String token,
            @PathVariable Long id) {
        return service.submit(auth.require(token), id);
    }

    @PostMapping("/api/applications/{id}/approve")
    public Application approve(
            @RequestHeader(value = "X-Token", defaultValue = "") String token,
            @PathVariable Long id,
            @RequestParam BigDecimal amount) {
        return service.approve(auth.require(token), id, amount);
    }

    @PostMapping("/api/applications/{id}/reject")
    public Application reject(
            @RequestHeader(value = "X-Token", defaultValue = "") String token,
            @PathVariable Long id,
            @RequestParam String reason) {
        return service.reject(auth.require(token), id, reason);
    }

    @PostMapping("/api/applications/{id}/cancel")
    public Application cancel(
            @RequestHeader(value = "X-Token", defaultValue = "") String token,
            @PathVariable Long id,
            @RequestParam String reason) {
        return service.cancel(auth.require(token), id, reason);
    }

    @PostMapping("/api/applications/{id}/settle")
    public Application settle(
            @RequestHeader(value = "X-Token", defaultValue = "") String token,
            @PathVariable Long id,
            @RequestParam BigDecimal actual) {
        return service.settle(auth.require(token), id, actual);
    }
}
