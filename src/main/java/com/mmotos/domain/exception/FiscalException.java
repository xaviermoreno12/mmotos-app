package com.mmotos.domain.exception;

public class FiscalException extends RuntimeException {
    public FiscalException(String message) { super(message); }
    public FiscalException(String message, Throwable cause) { super(message, cause); }
}
