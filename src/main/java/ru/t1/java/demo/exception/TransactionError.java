package ru.t1.java.demo.exception;

public class TransactionError extends RuntimeException {
    public TransactionError(String message) {
        super(message);
    }
}
