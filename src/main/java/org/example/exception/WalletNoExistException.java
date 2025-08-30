package org.example.exception;

public class WalletNoExistException extends RuntimeException {
    public WalletNoExistException(String message) {
        super(message);
    }
}
