package ar.odonto.turnos.domain.port;

import ar.odonto.turnos.domain.model.Solicitante;

/**
 * Identidad de quien hace la solicitud en curso. C-01 provee una implementación permisiva
 * (siempre {@link Solicitante#SISTEMA}); C-05 la reemplaza con la del usuario autenticado.
 */
public interface UsuarioActual {

    Solicitante solicitante();
}
