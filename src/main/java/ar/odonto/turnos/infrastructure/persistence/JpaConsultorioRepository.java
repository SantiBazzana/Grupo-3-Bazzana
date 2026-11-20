package ar.odonto.turnos.infrastructure.persistence;

import ar.odonto.turnos.domain.model.Consultorio;
import ar.odonto.turnos.domain.port.ConsultorioRepository;
import org.springframework.data.repository.Repository;

/** Implementación Spring Data del puerto {@link ConsultorioRepository} (design D3 de C-01). */
interface JpaConsultorioRepository extends Repository<Consultorio, Long>, ConsultorioRepository {
}
