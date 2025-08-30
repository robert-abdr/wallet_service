package org.example.util;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.example.exception.NotEnoughMoneyException;
import org.example.exception.WalletNoExistException;
import org.example.model.OperationType;
import org.example.model.Wallet;
import org.example.repository.WalletRepository;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WalletServiceValidator {
    public void checkWalletExists(UUID uuid, WalletRepository repository) {
        repository
                .findByUuidWithLock(uuid)
                .orElseThrow(
                        () -> new WalletNoExistException(String.format("Wallet with UUID: %s was not found", uuid)));
    }

    public void checkWalletBalanceAndLock(
            UUID uuid, WalletRepository repository, BigDecimal amount, OperationType operationType) {
        Wallet wallet = repository
                .findByUuidWithLock(uuid)
                .orElseThrow(
                        () -> new WalletNoExistException(String.format("Wallet with UUID: %s was not found", uuid)));

        if (operationType == OperationType.WITHDRAW) {
            if (wallet.getBalance().compareTo(amount) < 0) {
                String errorMessage = String.format(
                        """
                                Wallet with UUID: %s has not enough money to process, minimal balance must be: %s
                                """,
                        uuid, amount);
                log.error(errorMessage);
                throw new NotEnoughMoneyException(errorMessage);
            }
        }
    }
}
