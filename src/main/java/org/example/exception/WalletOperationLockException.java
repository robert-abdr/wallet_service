package org.example.exception;

public class WalletOperationLockException extends RuntimeException {
    public WalletOperationLockException(String message) {
        super(message);
    }
}
