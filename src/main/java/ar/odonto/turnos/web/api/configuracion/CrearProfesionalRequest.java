package ar.odonto.turnos.web.api.configuracion;

import jakarta.validation.constraints.NotBlank;

/** Cuerpo de {@code POST /api/profesionales}. Sin credenciales ni datos de usuario (decisión c). */
public record CrearProfesionalRequest(@NotBlank String nombre, @NotBlank String apellido,
                                      @NotBlank String matricula, String telefono) {
}
