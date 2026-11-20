package ar.odonto.turnos.infrastructure.persistence;

import ar.odonto.turnos.domain.model.Paciente;
import ar.odonto.turnos.domain.port.PacienteRepository;
import org.springframework.data.repository.Repository;

/** Implementación Spring Data del puerto {@link PacienteRepository} (design D3 de C-01). */
interface JpaPacienteRepository extends Repository<Paciente, Long>, PacienteRepository {
}
