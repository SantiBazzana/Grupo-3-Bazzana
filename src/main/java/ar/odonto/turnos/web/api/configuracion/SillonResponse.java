package ar.odonto.turnos.web.api.configuracion;

import ar.odonto.turnos.application.configuracion.SillonVista;

/** Representación HTTP de un sillón: sin entidades JPA (J2). */
public record SillonResponse(Long id, String nombre, boolean activo) {

    static SillonResponse de(SillonVista vista) {
        return new SillonResponse(vista.id(), vista.nombre(), vista.activo());
    }
}
