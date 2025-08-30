package org.example.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.model.OperationType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletRequestPostDto {
    @NotNull(message = "UUID must be not null and valid")
    private UUID walletUuid;

    @NotNull(message = "Operation Type must be like DEPOSIT|WITHDRAW")
    private OperationType operationType;

    @NotNull(message = "Amount must be not null")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    @Positive(message = "Amount must be positive")
    @DecimalMax(value = "1000000.00", message = "Amount must be maximum 1000000")
    @Digits(integer = 7, fraction = 2, message = "Amount maximum must have 7 integers and 2 fractions, like 999.00")
    private BigDecimal amount;
}
