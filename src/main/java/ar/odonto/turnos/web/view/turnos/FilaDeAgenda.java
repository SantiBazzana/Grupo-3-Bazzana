package ar.odonto.turnos.web.view.turnos;

import ar.odonto.turnos.application.agenda.TurnoDeAgendaVista;

import java.time.format.DateTimeFormatter;

/** Fila ya formateada de la agenda del día; deriva de la vista de salida de C-07 sin datos personales. */
public record FilaDeAgenda(Long id, String hora, String pacienteNombre, String prestacionNombre, String sillonNombre,
        boolean sobreturno) {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    static FilaDeAgenda de(TurnoDeAgendaVista turno) {
        return new FilaDeAgenda(turno.id(),
                HORA.format(turno.inicio()) + " – " + HORA.format(turno.fin()),
                turno.pacienteNombre(), turno.prestacionNombre(), turno.sillonNombre(), turno.sobreturno());
    }
}
