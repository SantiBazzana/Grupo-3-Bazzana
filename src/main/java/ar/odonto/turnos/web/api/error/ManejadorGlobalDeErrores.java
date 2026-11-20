package ar.odonto.turnos.web.api.error;

import ar.odonto.turnos.domain.error.CodigoError;
import ar.odonto.turnos.domain.error.ReglaDeNegocioException;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

/**
 * Traduce toda excepción a Problem Details (RFC 7807) con el campo {@code codigo} del catálogo {@link CodigoError}.
 * Ninguna respuesta incluye la traza ni datos personales de pacientes (spec base-del-sistema).
 */
@RestControllerAdvice
public class ManejadorGlobalDeErrores extends ResponseEntityExceptionHandler {

    static final String PROPIEDAD_CODIGO = "codigo";

    private static final Logger log = LoggerFactory.getLogger(ManejadorGlobalDeErrores.class);

    @ExceptionHandler(ReglaDeNegocioException.class)
    ResponseEntity<ProblemDetail> reglaDeNegocio(ReglaDeNegocioException ex) {
        return respuesta(ex.codigo(), ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ProblemDetail> integridad(DataIntegrityViolationException ex) {
        if (esRestriccionUnica(ex)) {
            return respuesta(CodigoError.DATO_DUPLICADO, CodigoError.DATO_DUPLICADO.titulo());
        }
        log.error("Violación de integridad no contemplada", ex);
        return respuesta(CodigoError.ERROR_INTERNO, CodigoError.ERROR_INTERNO.titulo());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> inesperado(Exception ex) {
        log.error("Error inesperado", ex);
        return respuesta(CodigoError.ERROR_INTERNO, CodigoError.ERROR_INTERNO.titulo());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<String> campos = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        ProblemDetail problema = problema(CodigoError.ENTRADA_INVALIDA,
                "Campos inválidos: " + String.join("; ", campos));
        problema.setProperty("campos", campos);
        return ResponseEntity.status(problema.getStatus()).headers(headers).body(problema);
    }

    /**
     * Cubre las excepciones de Spring MVC (JSON ilegible, tipo incorrecto, parámetro faltante, 404, 405...):
     * conserva su estado HTTP y agrega el {@code codigo} que corresponde a esa familia de errores.
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body, HttpHeaders headers,
            HttpStatusCode statusCode, WebRequest request) {
        ResponseEntity<Object> respuesta = super.handleExceptionInternal(ex, body, headers, statusCode, request);
        if (respuesta != null && respuesta.getBody() instanceof ProblemDetail problema
                && (problema.getProperties() == null || !problema.getProperties().containsKey(PROPIEDAD_CODIGO))) {
            problema.setProperty(PROPIEDAD_CODIGO, codigoPorEstado(statusCode.value()).name());
        }
        return respuesta;
    }

    static CodigoError codigoPorEstado(int estado) {
        return switch (estado) {
            case 401 -> CodigoError.NO_AUTENTICADO;
            case 403 -> CodigoError.ACCESO_DENEGADO;
            case 404 -> CodigoError.RECURSO_INEXISTENTE;
            default -> estado >= 500 ? CodigoError.ERROR_INTERNO : CodigoError.ENTRADA_INVALIDA;
        };
    }

    private static boolean esRestriccionUnica(Throwable ex) {
        for (Throwable causa = ex; causa != null; causa = causa.getCause()) {
            if (causa instanceof ConstraintViolationException violacion) {
                return violacion.getKind() == ConstraintViolationException.ConstraintKind.UNIQUE;
            }
        }
        return false;
    }

    private static ResponseEntity<ProblemDetail> respuesta(CodigoError codigo, String detalle) {
        ProblemDetail problema = problema(codigo, detalle);
        return ResponseEntity.status(problema.getStatus()).body(problema);
    }

    private static ProblemDetail problema(CodigoError codigo, String detalle) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(codigo.estadoHttp()), detalle);
        problema.setTitle(codigo.titulo());
        problema.setProperty(PROPIEDAD_CODIGO, codigo.name());
        return problema;
    }
}
