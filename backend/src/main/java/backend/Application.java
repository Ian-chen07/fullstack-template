package backend;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "application")
public class Application {

    @Id
    public Long id;

    public Long projectId;

    public String purpose;

    public BigDecimal amount;
    public BigDecimal approvedAmount;
    public BigDecimal actualAmount;

    public String status;
}