package ar.odonto.turnos.web.api.turnos;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/** Reloj de test que se puede mover entre escenarios (por ejemplo "ahora 08:00" y "ahora 11:00"). Zona AR. */
final class RelojAjustable extends Clock {

    static final ZoneId ZONA_AR = ZoneId.of("America/Argentina/Buenos_Aires");

    private volatile Instant instante;

    RelojAjustable(LocalDateTime ahora) {
        fijar(ahora);
    }

    void fijar(LocalDateTime ahora) {
        this.instante = ahora.atZone(ZONA_AR).toInstant();
    }

    @Override
    public ZoneId getZone() {
        return ZONA_AR;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return Clock.fixed(instante, zone);
    }

    @Override
    public Instant instant() {
        return instante;
    }
}
