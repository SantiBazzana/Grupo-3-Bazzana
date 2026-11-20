package ar.odonto.turnos.support;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Relojes fijos en la zona del consultorio para tests deterministas (RN-GL-02).
 */
public final class RelojDePrueba {

    public static final ZoneId ZONA_AR = ZoneId.of("America/Argentina/Buenos_Aires");

    private RelojDePrueba() {
    }

    /** Reloj detenido en la fecha y hora local de Argentina indicada, en formato ISO (por ejemplo "2026-11-23T09:00"). */
    public static Clock en(String fechaHoraLocal) {
        return en(LocalDateTime.parse(fechaHoraLocal));
    }

    public static Clock en(LocalDateTime fechaHoraLocal) {
        return Clock.fixed(fechaHoraLocal.atZone(ZONA_AR).toInstant(), ZONA_AR);
    }
}
