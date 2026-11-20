package ar.odonto.turnos.domain.port;

import ar.odonto.turnos.domain.model.Turno;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de persistencia de turnos. Todas las consultas de choque trabajan SOLO sobre turnos RESERVADO
 * (los CANCELADO nunca bloquean un horario, RN-CA-02).
 * <p>
 * Cómo aplicar el margen M (RN-TU-02/03, design D4 de C-01): un turno nuevo N choca con uno existente E si
 * {@code N.inicio < E.fin + M} y {@code E.inicio < N.fin + M}. Quien llama pasa
 * {@code desde = N.inicio - M} y {@code hasta = N.fin + M}; la consulta devuelve los E con
 * {@code E.fin > desde} y {@code E.inicio < hasta}.
 */
public interface TurnoRepository {

    Turno save(Turno turno);

    Optional<Turno> findById(Long id);

    long count();

    /** Turnos activos del profesional que chocan con [desde, hasta). {@code excluirTurnoId} puede ser null. */
    List<Turno> buscarChoquesDeProfesional(Long profesionalId, LocalDateTime desde, LocalDateTime hasta,
            Long excluirTurnoId);

    /** Turnos activos del sillón que chocan con [desde, hasta). {@code excluirTurnoId} puede ser null. */
    List<Turno> buscarChoquesDeSillon(Long sillonId, LocalDateTime desde, LocalDateTime hasta, Long excluirTurnoId);

    /** Turnos activos del paciente superpuestos con [inicio, fin), SIN margen (RN-TU-05). */
    List<Turno> buscarSuperpuestosDePaciente(Long pacienteId, LocalDateTime inicio, LocalDateTime fin,
            Long excluirTurnoId);

    /** Turnos activos del profesional con inicio en [desde, hasta), ordenados por inicio (agenda, RN-AG-01). */
    List<Turno> buscarActivosDeProfesionalEntre(Long profesionalId, LocalDateTime desde, LocalDateTime hasta);
}
