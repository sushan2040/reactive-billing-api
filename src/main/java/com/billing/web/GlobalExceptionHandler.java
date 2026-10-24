package com.billing.web;

import com.billing.error.InvalidInvoiceStateException;
import com.billing.error.InvoiceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Turns exceptions (including errors that travel through a Mono) into clean JSON responses. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvoiceNotFoundException.class)
    public ProblemDetail notFound(InvoiceNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Invoice not found", ex.getMessage());
    }

    @ExceptionHandler(InvalidInvoiceStateException.class)
    public ProblemDetail invalidState(InvalidInvoiceStateException ex) {
        return problem(HttpStatus.CONFLICT, "Invalid invoice state", ex.getMessage());
    }

    /** Thrown when @Valid fails on the request body. */
    @ExceptionHandler(WebExchangeBindException.class)
    public ProblemDetail validation(WebExchangeBindException ex) {
        ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "Validation failed", "Request body is invalid");
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fe : ex.getFieldErrors()) {
            errors.putIfAbsent(fe.getField(), Objects.toString(fe.getDefaultMessage(), "invalid"));
        }
        pd.setProperty("errors", errors);
        return pd;
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        return pd;
    }
}
