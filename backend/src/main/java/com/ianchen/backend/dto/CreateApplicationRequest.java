package com.ianchen.backend.dto;

import java.math.BigDecimal;

public record CreateApplicationRequest(String purpose, BigDecimal amount) {
}
