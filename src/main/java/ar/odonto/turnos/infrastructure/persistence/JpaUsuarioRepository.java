package ar.odonto.turnos.infrastructure.persistence;

import ar.odonto.turnos.domain.model.Usuario;
import ar.odonto.turnos.domain.port.UsuarioRepository;
import org.springframework.data.repository.Repository;

/** Implementación Spring Data del puerto {@link UsuarioRepository} (design D3 de C-01). */
interface JpaUsuarioRepository extends Repository<Usuario, Long>, UsuarioRepository {
}
