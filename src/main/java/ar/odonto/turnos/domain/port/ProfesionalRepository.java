package ar.odonto.turnos.domain.port;

import ar.odonto.turnos.domain.model.Profesional;

import java.util.List;
import java.util.Optional;

/** Puerto de persistencia de profesionales. */
public interface ProfesionalRepository {

    Profesional save(Profesional profesional);

    Optional<Profesional> findById(Long id);

    List<Profesional> findAll();

    long count();

    boolean existsByMatricula(String matricula);
}
