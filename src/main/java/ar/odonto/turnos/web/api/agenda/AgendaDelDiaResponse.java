package ar.odonto.turnos.web.api.agenda;

import ar.odonto.turnos.application.agenda.TurnoDeAgendaVista;

import java.time.LocalDate;
import java.util.List;

/** Respuesta de la agenda del día: fecha, profesional y lista de turnos (sin datos personales). */
public record AgendaDelDiaResponse(LocalDate fecha, Long profesionalId, List<TurnoDeAgendaResponse> turnos) {

    static AgendaDelDiaResponse de(LocalDate fecha, Long profesionalId, List<TurnoDeAgendaVista> turnos) {
        return new AgendaDelDiaResponse(fecha, profesionalId, turnos.stream().map(TurnoDeAgendaResponse::de).toList());
    }
}
