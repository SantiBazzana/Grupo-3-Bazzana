package ar.odonto.turnos.domain.port;

import ar.odonto.turnos.domain.model.HorarioAtencion;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

/** Puerto de persistencia del horario de atención. Sin franja para un día = consultorio cerrado ese día. */
public interface HorarioAtencionRepository {

    HorarioAtencion save(HorarioAtencion horario);

    List<HorarioAtencion> findAll();

    long count();

    Optional<HorarioAtencion> findByConsultorioIdAndDiaSemana(Long consultorioId, DayOfWeek diaSemana);
}
