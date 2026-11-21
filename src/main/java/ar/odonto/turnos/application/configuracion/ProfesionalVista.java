package ar.odonto.turnos.application.configuracion;

import ar.odonto.turnos.domain.model.Profesional;

/**
 * Datos de un profesional que salen del caso de uso; nunca la entidad JPA (J2) ni datos de usuario (decisión c).
 * Expone su {@code id} para que C-05 pueda vincularlo después con un usuario ODONTOLOGO.
 */
public record ProfesionalVista(Long id, String nombre, String apellido, String matricula, String telefono,
                               boolean activo) {

    static ProfesionalVista de(Profesional profesional) {
        return new ProfesionalVista(profesional.getId(), profesional.getNombre(), profesional.getApellido(),
                profesional.getMatricula(), profesional.getTelefono(), profesional.isActivo());
    }
}
