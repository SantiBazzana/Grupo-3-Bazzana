package ar.odonto.turnos.domain.port;

import ar.odonto.turnos.domain.model.Sillon;

import java.util.List;
import java.util.Optional;

/** Puerto de persistencia de sillones. */
public interface SillonRepository {

    Sillon save(Sillon sillon);

    Optional<Sillon> findById(Long id);

    List<Sillon> findAll();

    long count();

    boolean existsByNombre(String nombre);
}
