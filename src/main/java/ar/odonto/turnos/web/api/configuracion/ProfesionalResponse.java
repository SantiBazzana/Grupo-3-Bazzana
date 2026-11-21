package ar.odonto.turnos.web.api.configuracion;

import ar.odonto.turnos.application.configuracion.ProfesionalVista;

/** Representación HTTP de un profesional: no expone usuario, contraseña ni usuarioId (decisión c). */
public record ProfesionalResponse(Long id, String nombre, String apellido, String matricula, String telefono,
                                  boolean activo) {

    static ProfesionalResponse de(ProfesionalVista vista) {
        return new ProfesionalResponse(vista.id(), vista.nombre(), vista.apellido(), vista.matricula(),
                vista.telefono(), vista.activo());
    }
}
