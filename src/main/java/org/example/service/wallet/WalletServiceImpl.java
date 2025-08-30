package org.example.service.wallet;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.WalletBalanceResponseDto;
import org.example.dto.WalletRequestPostDto;
import org.example.exception.NotEnoughMoneyException;
import org.example.exception.WalletNoExistException;
import org.example.exception.WalletOperationLockException;
import org.example.mapper.WalletMapper;
import org.example.model.OperationType;
import org.example.repository.WalletRepository;
import org.example.service.WalletService;
import org.example.util.WalletServiceValidator;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.MethodArgumentNotValidException;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {
    private final WalletRepository repository;
    private final WalletServiceValidator validator;
    private final WalletMapper mapper;

    @Transactional
    @Override
    public void processWalletOperation(WalletRequestPostDto dto) {
        processWalletOperationForProxy(dto);
    }

    @Override
    @Transactional(readOnly = true)
    public WalletBalanceResponseDto getBalance(UUID uuid) {
        return mapper.toOutputDto(repository
                .findByUuid(uuid)
                .orElseThrow(
                        () -> new WalletNoExistException(String.format("Wallet with uuid: %s was not found", uuid))));
    }

    @Retryable(
            retryFor = {PessimisticLockingFailureException.class},
            notRecoverable = {
                NotEnoughMoneyException.class,
                WalletNoExistException.class,
                WalletOperationLockException.class,
                MethodArgumentNotValidException.class,
                IllegalArgumentException.class,
                InvalidFormatException.class
            },
            maxAttemptsExpression = "${retry.max_attempts}",
            backoff = @Backoff(delayExpression = "${retry.delay}", multiplierExpression = "${retry.multiplier}"))
    public void processWalletOperationForProxy(WalletRequestPostDto dto) {
        UUID uuid = dto.getWalletUuid();
        BigDecimal amount = dto.getAmount();

        validator.checkWalletExists(uuid, repository);
        log.debug("Starting process with wallet uuid: {}", uuid);
        if (dto.getOperationType() == OperationType.DEPOSIT) {
            log.debug("Operation type - DEPOSIT, starting deposit");
            repository.depositBalance(amount, uuid);
            log.debug("Deposit for wallet uuid:{} was completed", uuid);
        } else {
            validator.checkWalletBalanceAndLock(uuid, repository, amount, dto.getOperationType());
            try {
                log.debug("Operation type - WITHDRAW, starting withdraw");
                repository.withdrawBalance(amount, uuid);
                log.debug("Withdraw for wallet uuid: {} was completed", uuid);
            } catch (DataIntegrityViolationException e) {
                String errorMessage = "Wallet with uuid: %s have not enough money, you must have minimum %s rubles"
                        .formatted(uuid, amount);
                log.error(errorMessage, e);
                throw new NotEnoughMoneyException(errorMessage);
            }
        }
    }

    @Recover
    public void recoverProcessWalletOperation(PessimisticLockingFailureException e, WalletRequestPostDto dto) {
        String errorMessage = String.format(
                """
                        Failed to process wallet operation for wallet with uuid: %s after all retries
                        Reason: %s. Please try again later.
                        """,
                dto.getWalletUuid(), e.getMessage());
        log.error(errorMessage, e);
        throw new WalletOperationLockException(errorMessage);
    }
}
