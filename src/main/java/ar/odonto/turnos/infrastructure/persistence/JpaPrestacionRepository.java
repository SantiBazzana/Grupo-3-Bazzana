package ar.odonto.turnos.infrastructure.persistence;

import ar.odonto.turnos.domain.model.Prestacion;
import ar.odonto.turnos.domain.port.PrestacionRepository;
import org.springframework.data.repository.Repository;

/** Implementación Spring Data del puerto {@link PrestacionRepository} (design D3 de C-01). */
interface JpaPrestacionRepository extends Repository<Prestacion, Long>, PrestacionRepository {
}
