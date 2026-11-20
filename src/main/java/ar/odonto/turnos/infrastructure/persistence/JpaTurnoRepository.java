package ar.odonto.turnos.infrastructure.persistence;

import ar.odonto.turnos.domain.model.Turno;
import ar.odonto.turnos.domain.port.TurnoRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación Spring Data de {@link TurnoRepository} (design D3/D4 de C-01): consultas de choque en JPQL estándar,
 * solo sobre turnos RESERVADO. Ver la javadoc del puerto para cómo aplicar el margen.
 */
interface JpaTurnoRepository extends Repository<Turno, Long>, TurnoRepository {

    String ACTIVO = "t.estado = ar.odonto.turnos.domain.model.TurnoEstado.RESERVADO";
    String EXCLUIR = "(:excluirTurnoId is null or t.id <> :excluirTurnoId)";

    @Override
    @Query("select t from Turno t where t.profesional.id = :profesionalId and " + ACTIVO
            + " and t.fin > :desde and t.inicio < :hasta and " + EXCLUIR + " order by t.inicio")
    List<Turno> buscarChoquesDeProfesional(@Param("profesionalId") Long profesionalId,
            @Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta,
            @Param("excluirTurnoId") Long excluirTurnoId);

    @Override
    @Query("select t from Turno t where t.sillon.id = :sillonId and " + ACTIVO
            + " and t.fin > :desde and t.inicio < :hasta and " + EXCLUIR + " order by t.inicio")
    List<Turno> buscarChoquesDeSillon(@Param("sillonId") Long sillonId, @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta, @Param("excluirTurnoId") Long excluirTurnoId);

    @Override
    @Query("select t from Turno t where t.paciente.id = :pacienteId and " + ACTIVO
            + " and t.fin > :inicio and t.inicio < :fin and " + EXCLUIR + " order by t.inicio")
    List<Turno> buscarSuperpuestosDePaciente(@Param("pacienteId") Long pacienteId,
            @Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin,
            @Param("excluirTurnoId") Long excluirTurnoId);

    @Override
    @Query("select t from Turno t where t.profesional.id = :profesionalId and " + ACTIVO
            + " and t.inicio >= :desde and t.inicio < :hasta order by t.inicio")
    List<Turno> buscarActivosDeProfesionalEntre(@Param("profesionalId") Long profesionalId,
            @Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}
