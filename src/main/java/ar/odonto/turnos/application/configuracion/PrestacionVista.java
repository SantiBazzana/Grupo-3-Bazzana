package ar.odonto.turnos.application.configuracion;

import ar.odonto.turnos.domain.model.Prestacion;

/** Datos de una prestación que salen del caso de uso; nunca la entidad JPA (J2). */
public record PrestacionVista(Long id, String nombre, int duracionMinutos, boolean activa) {

    static PrestacionVista de(Prestacion prestacion) {
        return new PrestacionVista(prestacion.getId(), prestacion.getNombre(), prestacion.getDuracionMinutos(),
                prestacion.isActiva());
    }
}
