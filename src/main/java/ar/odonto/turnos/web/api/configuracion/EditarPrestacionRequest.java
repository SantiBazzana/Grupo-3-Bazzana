package ar.odonto.turnos.web.api.configuracion;

import jakarta.validation.constraints.Pattern;

/** Cuerpo de {@code PUT /api/prestaciones/{id}}; los campos ausentes conservan el valor actual. */
public record EditarPrestacionRequest(@Pattern(regexp = ".*\\S.*") String nombre, Integer duracionMinutos,
                                      Boolean activa) {
}
