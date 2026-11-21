package ar.odonto.turnos.web.api.configuracion;

import ar.odonto.turnos.application.configuracion.HorarioVista;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/** Representación HTTP de la franja de un día, con horas en formato {@code HH:mm}. */
public record HorarioDiaResponse(DayOfWeek diaSemana, String apertura, String cierre) {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("HH:mm");

    static HorarioDiaResponse de(HorarioVista vista) {
        return new HorarioDiaResponse(vista.diaSemana(), formatear(vista.apertura()), formatear(vista.cierre()));
    }

    private static String formatear(LocalTime hora) {
        return hora == null ? null : hora.format(FORMATO);
    }
}
