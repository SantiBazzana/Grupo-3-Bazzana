package ar.odonto.turnos.application.agenda;

import ar.odonto.turnos.domain.model.Turno;

import java.time.LocalDateTime;

/**
 * Vista de la agenda de un profesional para un día: los datos de presentación de un turno activo,
 * sin DNI ni teléfono del paciente (reglas D1/D2 de AGENTS.md).
 */
public record TurnoDeAgendaVista(Long id, LocalDateTime inicio, LocalDateTime fin, boolean sobreturno,
        Long pacienteId, String pacienteNombre, Long prestacionId, String prestacionNombre, Long sillonId,
        String sillonNombre, Long profesionalId) {

    static TurnoDeAgendaVista de(Turno turno) {
        return new TurnoDeAgendaVista(
                turno.getId(),
                turno.getInicio(),
                turno.getFin(),
                turno.isSobreturno(),
                turno.getPaciente().getId(),
                turno.getPaciente().getNombre() + " " + turno.getPaciente().getApellido(),
                turno.getPrestacion().getId(),
                turno.getPrestacion().getNombre(),
                turno.getSillon().getId(),
                turno.getSillon().getNombre(),
                turno.getProfesional().getId());
    }
}
