package ar.odonto.turnos.infrastructure.persistence;

import ar.odonto.turnos.domain.model.Profesional;
import ar.odonto.turnos.domain.port.ProfesionalRepository;
import org.springframework.data.repository.Repository;

/** Implementación Spring Data del puerto {@link ProfesionalRepository} (design D3 de C-01). */
interface JpaProfesionalRepository extends Repository<Profesional, Long>, ProfesionalRepository {
}
