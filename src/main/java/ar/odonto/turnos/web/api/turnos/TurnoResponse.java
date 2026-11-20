package ar.odonto.turnos.web.api.turnos;

import ar.odonto.turnos.application.turnos.TurnoVista;
import ar.odonto.turnos.domain.model.TurnoEstado;

import java.time.LocalDateTime;

/** Respuesta de la API de turnos. Sin DNI ni teléfono del paciente (reglas D1/D2 de AGENTS.md). */
public record TurnoResponse(Long id, Long pacienteId, Long profesionalId, Long sillonId, Long prestacionId,
        LocalDateTime inicio, LocalDateTime fin, boolean sobreturno, TurnoEstado estado, String creadoPor,
        LocalDateTime creadoEn) {

    static TurnoResponse de(TurnoVista v) {
        return new TurnoResponse(v.id(), v.pacienteId(), v.profesionalId(), v.sillonId(), v.prestacionId(),
                v.inicio(), v.fin(), v.sobreturno(), v.estado(), v.creadoPor(), v.creadoEn());
    }
}
