package com.example.financebackend.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferRequest {
    private Integer userId;
    private Integer fromAccountId;
    private Integer toAccountId;
    private BigDecimal amount;
    private LocalDate transferDate;
    private String note;
}
