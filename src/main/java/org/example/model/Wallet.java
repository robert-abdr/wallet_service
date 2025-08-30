package org.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "wallets")
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JsonProperty("wallet_id")
    private UUID uuid;

    @Column(name = "balance", precision = 10, scale = 2, nullable = false)
    @NotNull(message = "Balance must be not null")
    @Positive(message = "Balance must be positive")
    @DecimalMin(value = "0.00", message = "Balance must be at least 0.00")
    @DecimalMax(value = "1000000000.00", message = "Your balance is too large, max is 1000000000")
    private BigDecimal balance;
}
