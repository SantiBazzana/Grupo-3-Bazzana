package ar.odonto.turnos.domain.port;

import ar.odonto.turnos.domain.model.Paciente;

import java.util.List;
import java.util.Optional;

/** Puerto de persistencia de pacientes (datos personales, Ley 25.326). */
public interface PacienteRepository {

    Paciente save(Paciente paciente);

    Optional<Paciente> findById(Long id);

    List<Paciente> findAll();

    long count();

    boolean existsByDni(String dni);

    Optional<Paciente> findByDni(String dni);
}
