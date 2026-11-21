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
    public Application create(String role, String purpose, BigDecimal amount) {
        requireRole(role, "member", "只有成员可以提交申请");

        if (purpose == null || purpose.isBlank()) {
            throw error("请填写用途");
        }
        requirePositive(amount, "申请金额");
        requireTwoDecimals(amount, "申请金额");

        Application app = new Application();
        app.projectId = 1L;
        app.purpose = purpose;
        app.amount = amount;
        app.status = "DRAFT";
        return applications.save(app);
    }

    @Transactional
    public Application submit(String role, Long applicationId) {
        requireRole(role, "member", "只有成员可以提交申请");

        Application app = findApplication(applicationId);
        Project project = findProject(app);

        if (!"DRAFT".equals(app.status)) {
            throw error("只有草稿可以提交");
        }
        requirePositive(app.amount, "申请金额");
        requireTwoDecimals(app.amount, "申请金额");
        if (project.availableAmount.compareTo(app.amount) < 0) {
            throw error("可用经费不足");
        }

        project.availableAmount = project.availableAmount.subtract(app.amount);
        project.frozenAmount = project.frozenAmount.add(app.amount);
        app.status = "PENDING_L1";

        projects.save(project);
        return applications.save(app);
    }

    @Transactional
    public Application approve(String role, Long applicationId, BigDecimal amount) {
        requireRole(role, "teacher", "只有老师可以批准");

        Application app = findApplication(applicationId);
        Project project = findProject(app);

        if (!"PENDING_L1".equals(app.status)) {
            throw error("只有待审批的申请可以批准");
        }
        requirePositive(amount, "批准金额");
        requireTwoDecimals(amount, "批准金额");
        if (amount.compareTo(app.amount) > 0) {
            throw error("批准金额不能超过申请金额");
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
    public Application settle(String role, Long applicationId, BigDecimal actual) {
        requireRole(role, "finance", "只有财务可以结算");

        Application app = findApplication(applicationId);
        Project project = findProject(app);

        if (!"APPROVED".equals(app.status)) {
            throw error("只有已批准的申请可以结算");
        }
        if (actual == null || actual.signum() < 0) {
            throw error("实际花费不能为负数");
        }
        requireTwoDecimals(actual, "实际花费");
        if (actual.compareTo(app.approvedAmount) > 0) {
            throw error("实际花费不能超过批准金额");
        }

        project.frozenAmount = project.frozenAmount.subtract(app.approvedAmount);
        project.usedAmount = project.usedAmount.add(actual);
        project.availableAmount = project.availableAmount.add(app.approvedAmount.subtract(actual));
        app.actualAmount = actual;
        app.status = "SETTLED";

        projects.save(project);
        return applications.save(app);
    }

    @Transactional
    public Application reject(String role, Long applicationId, String reason) {
        requireRole(role, "teacher", "只有老师可以拒绝");

        Application app = findApplication(applicationId);
        Project project = findProject(app);

        if (!"PENDING_L1".equals(app.status)) {
            throw error("只有待审批的申请可以拒绝");
        }
        if (reason == null || reason.isBlank()) {
            throw error("请填写拒绝原因");
        }

        project.frozenAmount = project.frozenAmount.subtract(app.amount);
        project.availableAmount = project.availableAmount.add(app.amount);
        app.status = "REJECTED";
        app.reason = reason;

        projects.save(project);
        return applications.save(app);
    }

    @Transactional
    public Application cancel(String role, Long applicationId, String reason) {
        requireRole(role, "member", "只有成员可以取消自己的申请");

        Application app = findApplication(applicationId);
        Project project = findProject(app);

        if (!"PENDING_L1".equals(app.status) && !"APPROVED".equals(app.status)) {
            throw error("只有待审批或已批准的申请可以取消");
        }
        if (reason == null || reason.isBlank()) {
            throw error("请填写取消原因");
        }

        BigDecimal held = "APPROVED".equals(app.status) ? app.approvedAmount : app.amount;
        project.frozenAmount = project.frozenAmount.subtract(held);
        project.availableAmount = project.availableAmount.add(held);
        app.status = "CANCELED";
        app.reason = reason;

        projects.save(project);
        return applications.save(app);
    }

    private void requireRole(String role, String expected, String message) {
        if (!expected.equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, message);
        }
    }

    private Application findApplication(Long id) {
        return applications.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "申请不存在"));
    }

    private Project findProject(Application app) {
        return projects.findById(app.projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "项目不存在"));
    }

    private void requirePositive(BigDecimal value, String label) {
        if (value == null || value.signum() <= 0) {
            throw error(label + "必须大于 0");
        }
    }

    private void requireTwoDecimals(BigDecimal value, String label) {
        if (value.scale() > 2) {
            throw error(label + "最多两位小数");
        }
    }

    private ResponseStatusException error(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
