package ar.odonto.turnos.application.turnos;

import ar.odonto.turnos.domain.model.Paciente;
import ar.odonto.turnos.domain.model.Profesional;
import ar.odonto.turnos.domain.model.Sillon;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Bloqueo pesimista ({@code SELECT ... FOR UPDATE}) con JPA. Vive en {@code application/turnos} para no tocar
 * archivos compartidos de C-01 (desviación aceptada en el design D4 de C-02). Requiere una transacción activa.
 */
@Component
class JpaBloqueoDeRecursos implements BloqueoDeRecursos {

    private final EntityManager entityManager;

    JpaBloqueoDeRecursos(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Optional<Sillon> bloquearSillon(Long id) {
        return bloquear(Sillon.class, id);
    }

    @Override
    public Optional<Profesional> bloquearProfesional(Long id) {
        return bloquear(Profesional.class, id);
    }

    @Override
    public Optional<Paciente> bloquearPaciente(Long id) {
        return bloquear(Paciente.class, id);
    }

    private <T> Optional<T> bloquear(Class<T> tipo, Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(entityManager.find(tipo, id, LockModeType.PESSIMISTIC_WRITE));
    }
}
