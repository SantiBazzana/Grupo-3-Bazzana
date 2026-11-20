package ar.odonto.turnos.application.turnos;

import ar.odonto.turnos.domain.model.Turno;
import ar.odonto.turnos.domain.model.TurnoEstado;

import java.time.LocalDateTime;

/**
 * Datos de un turno para afuera del caso de uso. Se arma DENTRO de la transacción (open-in-view desactivado)
 * y no incluye DNI ni teléfono del paciente (reglas D1/D2 de AGENTS.md).
 */
public record TurnoVista(Long id, Long pacienteId, Long profesionalId, Long sillonId, Long prestacionId,
        LocalDateTime inicio, LocalDateTime fin, boolean sobreturno, TurnoEstado estado, String creadoPor,
        LocalDateTime creadoEn) {

    static TurnoVista de(Turno t) {
        return new TurnoVista(t.getId(), t.getPaciente().getId(), t.getProfesional().getId(), t.getSillon().getId(),
                t.getPrestacion().getId(), t.getInicio(), t.getFin(), t.isSobreturno(), t.getEstado(),
                t.getCreadoPor(), t.getCreadoEn());
    }
}
