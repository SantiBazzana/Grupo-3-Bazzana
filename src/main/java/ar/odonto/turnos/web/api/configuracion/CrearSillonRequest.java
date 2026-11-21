package ar.odonto.turnos.web.api.configuracion;

import jakarta.validation.constraints.NotBlank;

/** Cuerpo de {@code POST /api/sillones}. */
public record CrearSillonRequest(@NotBlank String nombre) {
}
