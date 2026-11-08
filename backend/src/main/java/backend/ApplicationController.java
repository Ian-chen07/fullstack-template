package backend;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

    @PostMapping("/api/applications/{id}/submit")
    public Application submit(@PathVariable Long id) {
        return service.submit(id);
    }

    @PostMapping("/api/applications/{id}/approve")
    public Application approve(@PathVariable Long id, @RequestParam BigDecimal amount) {
        return service.approve(id, amount);
    }

    @PostMapping("/api/applications/{id}/settle")
    public Application settle(@PathVariable Long id, @RequestParam BigDecimal actual) {
        return service.settle(id, actual);
    }
}