package backend;

import java.math.BigDecimal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BudgetService {

    private final ApplicationRepository applications;
    private final ProjectRepository projects;

    public BudgetService(ApplicationRepository applications, ProjectRepository projects) {
        this.applications = applications;
        this.projects = projects;
    }

    @Transactional
    public Application submit(Long applicationId) {
        Application app = applications.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "申请不存在"));

        Project project = projects.findById(app.projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "项目不存在"));

        if (!"DRAFT".equals(app.status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "只有草稿可以提交");
        }
        if (app.amount == null || app.amount.signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "金额必须大于 0");
        }
        if (project.availableAmount.compareTo(app.amount) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "可用经费不足");
        }

        project.availableAmount = project.availableAmount.subtract(app.amount);
        project.frozenAmount = project.frozenAmount.add(app.amount);
        app.status = "PENDING_L1";

        projects.save(project);
        return applications.save(app);
    }

    @Transactional
    public Application approve(Long applicationId, BigDecimal amount) {
        Application app = applications.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "申请不存在"));

        Project project = projects.findById(app.projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "项目不存在"));

        if (!"PENDING_L1".equals(app.status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "只有待审批的申请可以批准");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "批准金额必须大于 0");
        }
        if (amount.compareTo(app.amount) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "批准金额不能超过申请金额");
        }

        BigDecimal diff = app.amount.subtract(amount);
        project.availableAmount = project.availableAmount.add(diff);
        project.frozenAmount = project.frozenAmount.subtract(diff);
        app.approvedAmount = amount;
        app.status = "APPROVED";

        projects.save(project);
        return applications.save(app);
    }

    @Transactional
    public Application settle(Long applicationId, BigDecimal actual) {
        Application app = applications.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "申请不存在"));

        Project project = projects.findById(app.projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "项目不存在"));

        if (!"APPROVED".equals(app.status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "只有已批准的申请可以结算");
        }
        if (actual == null || actual.signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "实际花费不能为负数");
        }
        if (actual.compareTo(app.approvedAmount) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "实际花费不能超过批准金额");
        }

        project.frozenAmount = project.frozenAmount.subtract(app.approvedAmount);
        project.usedAmount = project.usedAmount.add(actual);
        project.availableAmount = project.availableAmount.add(app.approvedAmount.subtract(actual));
        app.actualAmount = actual;
        app.status = "SETTLED";

        projects.save(project);
        return applications.save(app);
    }
}