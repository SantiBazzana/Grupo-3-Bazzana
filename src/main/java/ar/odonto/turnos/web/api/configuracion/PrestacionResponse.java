package ar.odonto.turnos.web.api.configuracion;

import ar.odonto.turnos.application.configuracion.PrestacionVista;

/** Representación HTTP de una prestación: sin entidades JPA (J2). */
public record PrestacionResponse(Long id, String nombre, int duracionMinutos, boolean activa) {

    static PrestacionResponse de(PrestacionVista vista) {
        return new PrestacionResponse(vista.id(), vista.nombre(), vista.duracionMinutos(), vista.activa());
    }
}
