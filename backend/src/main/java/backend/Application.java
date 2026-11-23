package backend;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "application")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public Long projectId;

    public Long applicantId;

    public String purpose;

    public BigDecimal amount;
    public BigDecimal approvedAmount;
    public BigDecimal actualAmount;

    public String status;

    public String reason;
}
