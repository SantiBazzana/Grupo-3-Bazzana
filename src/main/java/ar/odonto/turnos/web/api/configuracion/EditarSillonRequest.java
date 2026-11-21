package ar.odonto.turnos.web.api.configuracion;

/** Cuerpo de {@code PUT /api/sillones/{id}}; los campos ausentes conservan el valor actual. */
public record EditarSillonRequest(String nombre, Boolean activo) {
}
