package ar.odonto.turnos.application.agenda;

import ar.odonto.turnos.domain.error.CodigoError;
import ar.odonto.turnos.domain.error.ReglaDeNegocioException;
import ar.odonto.turnos.domain.model.Solicitante;
import ar.odonto.turnos.domain.port.TurnoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Caso de uso "consultar la agenda del día" (US-020, RN-AG-01/02, design D1-D4 de C-07).
 * Devuelve los turnos activos del profesional cuyo inicio cae en la fecha pedida, en la zona del
 * consultorio, ordenados por inicio, con sus datos de presentación.
 */
@Service
public class ConsultarAgenda {

    private final TurnoRepository turnos;
    private final Clock reloj;

    public ConsultarAgenda(TurnoRepository turnos, Clock reloj) {
        this.turnos = turnos;
        this.reloj = reloj;
    }

    @Transactional(readOnly = true)
    public List<TurnoDeAgendaVista> ejecutar(Long profesionalId, LocalDate fecha, Solicitante solicitante) {
        validarVisibilidad(solicitante, profesionalId);
        LocalDateTime desde = fecha.atStartOfDay(reloj.getZone()).toLocalDateTime();
        LocalDateTime hasta = desde.plusDays(1);
        return turnos.buscarActivosDeProfesionalEntre(profesionalId, desde, hasta)
                .stream()
                .map(TurnoDeAgendaVista::de)
                .toList();
    }

    private void validarVisibilidad(Solicitante solicitante, Long profesionalId) {
        if (solicitante.esOdontologo()
                && (solicitante.profesionalId() == null || !solicitante.profesionalId().equals(profesionalId))) {
            throw new ReglaDeNegocioException(CodigoError.ACCESO_DENEGADO);
        }
    }
}
