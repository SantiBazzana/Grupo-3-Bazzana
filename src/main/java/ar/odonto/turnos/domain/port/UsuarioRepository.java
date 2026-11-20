package ar.odonto.turnos.domain.port;

import ar.odonto.turnos.domain.model.Usuario;

import java.util.List;
import java.util.Optional;

/** Puerto de persistencia de usuarios (lo usa C-05 para la autenticación). */
public interface UsuarioRepository {

    Usuario save(Usuario usuario);

    Optional<Usuario> findById(Long id);

    List<Usuario> findAll();

    long count();

    boolean existsByUsername(String username);

    Optional<Usuario> findByUsername(String username);
}
