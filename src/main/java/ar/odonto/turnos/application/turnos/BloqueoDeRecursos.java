package ar.odonto.turnos.application.turnos;

import ar.odonto.turnos.domain.model.Paciente;
import ar.odonto.turnos.domain.model.Profesional;
import ar.odonto.turnos.domain.model.Sillon;

import java.util.Optional;

/**
 * Carga recursos con bloqueo pesimista dentro de la transacción en curso (RN-TU-10, design D4 de C-02).
 * Se usa SIEMPRE en el orden sillón → profesional → paciente para evitar deadlocks.
 * Puerto propio de C-02 (anti-colisión 4): no modifica los puertos compartidos de C-01.
 */
public interface BloqueoDeRecursos {

    Optional<Sillon> bloquearSillon(Long id);

    Optional<Profesional> bloquearProfesional(Long id);

    Optional<Paciente> bloquearPaciente(Long id);
}
