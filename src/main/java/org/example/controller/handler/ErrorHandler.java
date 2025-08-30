package org.example.controller.handler;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.ErrorResponseDto;
import org.example.exception.NotEnoughMoneyException;
import org.example.exception.WalletNoExistException;
import org.example.exception.WalletOperationLockException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @ExceptionHandler(WalletNoExistException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponseDto handleNotExists(WalletNoExistException e) {
        log.error("Wallet not exists was thrown", e);
        return new ErrorResponseDto(
                HttpStatus.NOT_FOUND.name(),
                "Required wallet not exists",
                e.getMessage(),
                LocalDateTime.now().format(formatter));
    }

    @ExceptionHandler(NotEnoughMoneyException.class)
    @ResponseStatus(HttpStatus.PAYMENT_REQUIRED)
    public ErrorResponseDto handleNoEnoughMoney(NotEnoughMoneyException e) {
        log.error("Not enough Money on wallet", e);
        return new ErrorResponseDto(
                HttpStatus.PAYMENT_REQUIRED.name(),
                "This wallet have not enough money",
                e.getMessage(),
                LocalDateTime.now().format(formatter));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDto handleValidationException(MethodArgumentNotValidException e) {
        log.error("Not valid request DTO", e);
        return new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.name(),
                "Invalid DTO body in request",
                e.getMessage(),
                LocalDateTime.now().format(formatter));
    }

    @ExceptionHandler(WalletOperationLockException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public ErrorResponseDto handleLockException(WalletOperationLockException e) {
        log.error("Lock conflict, too many retries", e);
        return new ErrorResponseDto(
                HttpStatus.TOO_MANY_REQUESTS.name(),
                "Too many retries, pessimistic lock conflict",
                e.getMessage(),
                LocalDateTime.now().format(formatter));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponseDto handleException(Exception e) {
        log.error("Exception was thrown", e);
        return new ErrorResponseDto(
                HttpStatus.INTERNAL_SERVER_ERROR.name(),
                "Something get wrong.",
                e.getMessage(),
                LocalDateTime.now().format(formatter));
    }

    @ExceptionHandler(InvalidFormatException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDto handleInvalidFormat(InvalidFormatException e) {
        log.error("Invalid format of Request Body", e);
        return new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.name(),
                "Invalid format of Request Body",
                e.getMessage(),
                LocalDateTime.now().format(formatter));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDto handleNotReadable(InvalidFormatException e) {
        log.error("Unreadable format of Request Body", e);
        return new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.name(),
                "Unreadable format of Request Body",
                e.getMessage(),
                LocalDateTime.now().format(formatter));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDto handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.error("Invalid argument type", e);
        return new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.name(),
                "Invalid argument type",
                e.getMessage(),
                LocalDateTime.now().format(formatter));
    }
}
