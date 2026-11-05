package backend;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "project")
public class Project {

    @Id
    public Long id;

    public String name;

    public BigDecimal totalAmount;
    public BigDecimal availableAmount;
    public BigDecimal frozenAmount;
    public BigDecimal usedAmount;
}