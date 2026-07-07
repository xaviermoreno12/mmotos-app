package com.mmotos.infrastructure.input.rest;

import com.mmotos.domain.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InsufficientStockException.class)
    public ProblemDetail handleStockInsuficiente(InsufficientStockException ex) {
        return problem(HttpStatus.CONFLICT, "Stock insuficiente", ex.getMessage());
    }

    @ExceptionHandler(HardwareFailureException.class)
    public ProblemDetail handleHardware(HardwareFailureException ex) {
        log.warn("Hardware fiscal no disponible: {}", ex.getMessage());
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "Hardware fiscal no disponible", ex.getMessage());
    }

    @ExceptionHandler(FiscalValidationException.class)
    public ProblemDetail handleFiscalValidation(FiscalValidationException ex) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Error de validación fiscal", ex.getMessage());
    }

    @ExceptionHandler(FiscalException.class)
    public ProblemDetail handleFiscal(FiscalException ex) {
        log.error("Error fiscal: {}", ex.getMessage(), ex);
        return problem(HttpStatus.BAD_GATEWAY, "Error de comunicación fiscal", ex.getMessage());
    }

    @ExceptionHandler(PaymentInconsistencyException.class)
    public ProblemDetail handlePagos(PaymentInconsistencyException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Inconsistencia en pagos", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errores = ex.getBindingResult().getFieldErrors().stream()
            .collect(Collectors.toMap(FieldError::getField,
                f -> f.getDefaultMessage() != null ? f.getDefaultMessage() : "inválido",
                (a, b) -> a));

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Datos de entrada inválidos");
        pd.setProperty("errores", errores);
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    // Recursos estáticos inexistentes (favicon, devtools) → 404 silencioso
    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail handleNoResource(NoResourceFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage());
    }

    // Violación de constraint único (SKU duplicado, username duplicado, etc.)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleIntegridad(DataIntegrityViolationException ex) {
        log.warn("Violación de integridad: {}", ex.getMostSpecificCause().getMessage());
        String msg = ex.getMostSpecificCause().getMessage();
        if (msg != null && msg.contains("sku")) return problem(HttpStatus.CONFLICT, "SKU duplicado", "Ya existe un producto con ese SKU");
        if (msg != null && msg.contains("username")) return problem(HttpStatus.CONFLICT, "Usuario duplicado", "El nombre de usuario ya está en uso");
        if (msg != null && msg.contains("codigo")) return problem(HttpStatus.CONFLICT, "Código duplicado", "Ya existe un método de pago con ese código");
        return problem(HttpStatus.CONFLICT, "Conflicto de datos", "Ya existe un registro con ese valor único");
    }

    // ResponseStatusException (409 conflicto, 404, etc.) → muestra el mensaje real
    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail handleResponseStatus(ResponseStatusException ex) {
        var status = HttpStatus.valueOf(ex.getStatusCode().value());
        return problem(status, status.getReasonPhrase(), ex.getReason() != null ? ex.getReason() : ex.getMessage());
    }

    // @PreAuthorize deniega el acceso a un usuario autenticado → 403
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        return problem(HttpStatus.FORBIDDEN, "Acceso denegado", "No tiene permisos para realizar esta operación");
    }

    // Parámetro requerido ausente en la query string → 400
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingParam(MissingServletRequestParameterException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Parámetro requerido faltante",
            "El parámetro '" + ex.getParameterName() + "' es obligatorio");
    }

    // Cualquier otro error → 500
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenerico(Exception ex) {
        log.error("Error no controlado: {}", ex.getMessage(), ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor",
            "Contacte al administrador del sistema");
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }
}
