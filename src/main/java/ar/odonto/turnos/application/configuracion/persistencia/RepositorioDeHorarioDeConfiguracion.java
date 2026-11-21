package ar.odonto.turnos.application.configuracion.persistencia;

import ar.odonto.turnos.domain.model.HorarioAtencion;
import org.springframework.data.repository.Repository;

import java.time.DayOfWeek;
import java.util.Optional;

/**
 * Persistencia acotada de C-03 para cerrar un día (decisión b). El puerto compartido
 * {@code HorarioAtencionRepository} no expone {@code delete} y no se puede tocar (E1).
 */
public interface RepositorioDeHorarioDeConfiguracion extends Repository<HorarioAtencion, Long> {

    Optional<HorarioAtencion> findByConsultorioIdAndDiaSemana(Long consultorioId, DayOfWeek diaSemana);

    void delete(HorarioAtencion horario);
}
