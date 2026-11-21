package ar.odonto.turnos.web.api.configuracion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Cuerpo de {@code POST /api/prestaciones}. La duración válida es regla de negocio (422), no formato. */
public record CrearPrestacionRequest(@NotBlank String nombre, @NotNull Integer duracionMinutos) {
}
