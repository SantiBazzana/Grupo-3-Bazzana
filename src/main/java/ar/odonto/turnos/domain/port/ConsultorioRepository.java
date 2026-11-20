package ar.odonto.turnos.domain.port;

import ar.odonto.turnos.domain.model.Consultorio;

import java.util.List;
import java.util.Optional;

/** Puerto de persistencia del consultorio (uno solo en la v1). */
public interface ConsultorioRepository {

    Consultorio save(Consultorio consultorio);

    Optional<Consultorio> findById(Long id);

    List<Consultorio> findAll();

    long count();
}
