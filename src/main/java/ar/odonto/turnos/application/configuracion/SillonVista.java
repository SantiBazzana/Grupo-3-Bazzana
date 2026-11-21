package ar.odonto.turnos.application.configuracion;

import ar.odonto.turnos.domain.model.Sillon;

/** Datos de un sillón que salen del caso de uso; nunca la entidad JPA (J2). */
public record SillonVista(Long id, String nombre, boolean activo) {

    static SillonVista de(Sillon sillon) {
        return new SillonVista(sillon.getId(), sillon.getNombre(), sillon.isActivo());
    }
}
