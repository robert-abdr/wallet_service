package org.example.service;

import java.util.UUID;
import org.example.dto.WalletBalanceResponseDto;
import org.example.dto.WalletRequestPostDto;

public interface WalletService {
    void processWalletOperation(WalletRequestPostDto dto);

    void processWalletOperationForProxy(WalletRequestPostDto dto);

    WalletBalanceResponseDto getBalance(UUID uuid);
}
