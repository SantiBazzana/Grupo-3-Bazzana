package ar.odonto.turnos.web.api.configuracion;

/**
 * Cuerpo de {@code PUT /api/profesionales/{id}}. La matrícula se acepta solo para confirmar la actual;
 * cualquier otro valor se rechaza con {@code ENTRADA_INVALIDA} en el caso de uso.
 */
public record EditarProfesionalRequest(String nombre, String apellido, String matricula, String telefono,
                                       Boolean activo) {
}
