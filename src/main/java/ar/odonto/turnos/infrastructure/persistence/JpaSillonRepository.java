package ar.odonto.turnos.infrastructure.persistence;

import ar.odonto.turnos.domain.model.Sillon;
import ar.odonto.turnos.domain.port.SillonRepository;
import org.springframework.data.repository.Repository;

/** Implementación Spring Data del puerto {@link SillonRepository} (design D3 de C-01). */
interface JpaSillonRepository extends Repository<Sillon, Long>, SillonRepository {
}
