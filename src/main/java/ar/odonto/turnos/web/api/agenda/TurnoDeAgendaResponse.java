package ar.odonto.turnos.web.api.agenda;

import ar.odonto.turnos.application.agenda.TurnoDeAgendaVista;

import java.time.LocalDateTime;

/** Respuesta de un turno dentro de la agenda del día. Sin DNI ni teléfono del paciente. */
public record TurnoDeAgendaResponse(Long id, LocalDateTime inicio, LocalDateTime fin, boolean sobreturno,
        Long profesionalId, Long pacienteId, String pacienteNombre, Long prestacionId, String prestacionNombre,
        Long sillonId, String sillonNombre) {

    static TurnoDeAgendaResponse de(TurnoDeAgendaVista vista) {
        return new TurnoDeAgendaResponse(vista.id(), vista.inicio(), vista.fin(), vista.sobreturno(),
                vista.profesionalId(), vista.pacienteId(), vista.pacienteNombre(), vista.prestacionId(),
                vista.prestacionNombre(), vista.sillonId(), vista.sillonNombre());
    }
}
