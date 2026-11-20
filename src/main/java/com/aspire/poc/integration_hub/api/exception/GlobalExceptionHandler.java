
package com.aspire.poc.integration_hub.api.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ProblemDetail handleOrderNotFound(
            OrderNotFoundException exception,
            HttpServletRequest request) {

        log.warn("Order not found: {}", exception.getMessage());

        return buildProblem(
                HttpStatus.NOT_FOUND,
                "Order Not Found",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        String details = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": "
                        + error.getDefaultMessage())
                .findFirst()
                .orElse("Request validation failed");

        log.warn("Request validation failed: {}", details);

        return buildProblem(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                details,
                request
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleInvalidRequestBody(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {

        log.warn("Invalid request body: {}", exception.getMessage());

        return buildProblem(
                HttpStatus.BAD_REQUEST,
                "Invalid Request Body",
                "Request body is missing or contains invalid JSON.",
                request
        );
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ProblemDetail handleMissingHeader(
            MissingRequestHeaderException exception,
            HttpServletRequest request) {

        log.warn("Required request header is missing: {}",
                exception.getHeaderName());

        return buildProblem(
                HttpStatus.BAD_REQUEST,
                "Missing Required Header",
                "Required header is missing: "
                        + exception.getHeaderName(),
                request
        );
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ProblemDetail handleResourceAccess(
            ResourceAccessException exception,
            HttpServletRequest request) {

        log.error("Downstream service connection or timeout error",
                exception);

        return buildProblem(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Downstream Service Unavailable",
                "A downstream service is currently unavailable. Please try again later.",
                request
        );
    }

    @ExceptionHandler(RestClientResponseException.class)
    public ProblemDetail handleDownstreamResponse(
            RestClientResponseException exception,
            HttpServletRequest request) {

        log.error("Downstream service returned HTTP status: {}",
                exception.getStatusCode().value(), exception);

        return buildProblem(
                HttpStatus.BAD_GATEWAY,
                "Downstream Service Error",
                "A downstream service returned an unsuccessful response.",
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(
            Exception exception,
            HttpServletRequest request) {

        log.error("Unexpected application error", exception);

        return buildProblem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred. Please contact support if the problem persists.",
                request
        );
    }

    private ProblemDetail buildProblem(
            HttpStatus status,
            String title,
            String detail,
            HttpServletRequest request) {

        ProblemDetail problem = ProblemDetail.forStatus(status);

        problem.setTitle(title);
        problem.setDetail(detail);
        problem.setInstance(
                java.net.URI.create(request.getRequestURI())
        );

        Object correlationId =
                request.getAttribute("correlationId");

        if (correlationId != null) {
            problem.setProperty("correlationId", correlationId);
        }

        return problem;
    }

    @ExceptionHandler(CallNotPermittedException.class)
    public ProblemDetail handleCircuitBreakerOpen(
            CallNotPermittedException exception,
            HttpServletRequest request) {

        log.warn("Payment circuit breaker is open: {}",
                exception.getMessage());

        return buildProblem(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Payment Service Temporarily Unavailable",
                "Payment service is temporarily unavailable. Please try again later.",
                request
        );
    }
}
