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

    public ApplicationController(ApplicationRepository repository, BudgetService service) {
        this.repository = repository;
        this.service = service;
    }

    @GetMapping("/api/applications")
    public List<Application> list() {
        return repository.findAll();
    }

    @PostMapping("/api/applications")
    public Application create(
            @RequestHeader(value = "X-Role", defaultValue = "") String role,
            @RequestParam String purpose,
            @RequestParam BigDecimal amount) {
        return service.create(role, purpose, amount);
    }

    @PostMapping("/api/applications/{id}/submit")
    public Application submit(
            @RequestHeader(value = "X-Role", defaultValue = "") String role,
            @PathVariable Long id) {
        return service.submit(role, id);
    }

    @PostMapping("/api/applications/{id}/approve")
    public Application approve(
            @RequestHeader(value = "X-Role", defaultValue = "") String role,
            @PathVariable Long id,
            @RequestParam BigDecimal amount) {
        return service.approve(role, id, amount);
    }

    @PostMapping("/api/applications/{id}/settle")
    public Application settle(
            @RequestHeader(value = "X-Role", defaultValue = "") String role,
            @PathVariable Long id,
            @RequestParam BigDecimal actual) {
        return service.settle(role, id, actual);
    }

    @PostMapping("/api/applications/{id}/reject")
    public Application reject(
            @RequestHeader(value = "X-Role", defaultValue = "") String role,
            @PathVariable Long id,
            @RequestParam String reason) {
        return service.reject(role, id, reason);
    }

    @PostMapping("/api/applications/{id}/cancel")
    public Application cancel(
            @RequestHeader(value = "X-Role", defaultValue = "") String role,
            @PathVariable Long id,
            @RequestParam String reason) {
        return service.cancel(role, id, reason);
    }
}
