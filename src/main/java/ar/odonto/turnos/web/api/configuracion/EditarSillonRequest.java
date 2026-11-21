package ar.odonto.turnos.web.api.configuracion;

import jakarta.validation.constraints.Pattern;

/** Cuerpo de {@code PUT /api/sillones/{id}}; los campos ausentes conservan el valor actual. */
public record EditarSillonRequest(@Pattern(regexp = ".*\\S.*") String nombre, Boolean activo) {
}
