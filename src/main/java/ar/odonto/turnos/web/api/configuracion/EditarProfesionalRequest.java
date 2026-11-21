package ar.odonto.turnos.web.api.configuracion;

import jakarta.validation.constraints.Pattern;

/**
 * Cuerpo de {@code PUT /api/profesionales/{id}}. La matrícula se acepta solo para confirmar la actual;
 * cualquier otro valor se rechaza con {@code ENTRADA_INVALIDA} en el caso de uso.
 */
public record EditarProfesionalRequest(@Pattern(regexp = ".*\\S.*") String nombre,
                                       @Pattern(regexp = ".*\\S.*") String apellido,
                                       @Pattern(regexp = ".*\\S.*") String matricula,
                                       @Pattern(regexp = ".*\\S.*") String telefono,
                                       Boolean activo) {
}
