package org.example.controller.wallet;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.example.dto.WalletBalanceResponseDto;
import org.example.dto.WalletRequestPostDto;
import org.example.service.WalletService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    @PostMapping("/wallet")
    public ResponseEntity<String> processWalletOperation(@RequestBody @Valid WalletRequestPostDto dto) {
        walletService.processWalletOperation(dto);
        return ResponseEntity.ok("Operation with wallet success");
    }

    @GetMapping("wallets/{walletUuid}")
    public ResponseEntity<WalletBalanceResponseDto> getBalance(@PathVariable("walletUuid") UUID uuid) {
        return new ResponseEntity<>(walletService.getBalance(uuid), HttpStatus.OK);
    }
}
