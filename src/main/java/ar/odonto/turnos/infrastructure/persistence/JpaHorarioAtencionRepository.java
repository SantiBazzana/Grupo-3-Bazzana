package ar.odonto.turnos.infrastructure.persistence;

import ar.odonto.turnos.domain.model.HorarioAtencion;
import ar.odonto.turnos.domain.port.HorarioAtencionRepository;
import org.springframework.data.repository.Repository;

/** Implementación Spring Data del puerto {@link HorarioAtencionRepository} (design D3 de C-01). */
interface JpaHorarioAtencionRepository extends Repository<HorarioAtencion, Long>, HorarioAtencionRepository {
}
