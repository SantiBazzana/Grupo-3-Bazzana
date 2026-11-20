package ar.odonto.turnos.domain.port;

import ar.odonto.turnos.domain.model.Prestacion;

import java.util.List;
import java.util.Optional;

/** Puerto de persistencia de prestaciones. */
public interface PrestacionRepository {

    Prestacion save(Prestacion prestacion);

    Optional<Prestacion> findById(Long id);

    List<Prestacion> findAll();

    long count();

    boolean existsByNombre(String nombre);
}
