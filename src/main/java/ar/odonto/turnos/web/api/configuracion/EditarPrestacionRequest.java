package ar.odonto.turnos.web.api.configuracion;

/** Cuerpo de {@code PUT /api/prestaciones/{id}}; los campos ausentes conservan el valor actual. */
public record EditarPrestacionRequest(String nombre, Integer duracionMinutos, Boolean activa) {
}
