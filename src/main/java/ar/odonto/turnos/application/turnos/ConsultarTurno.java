package ar.odonto.turnos.application.turnos;

import ar.odonto.turnos.domain.error.CodigoError;
import ar.odonto.turnos.domain.error.ReglaDeNegocioException;
import ar.odonto.turnos.domain.port.TurnoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Caso de uso "consultar un turno por id" (spec turnos, requisito "Consultar un turno"). */
@Service
public class ConsultarTurno {

    private final TurnoRepository turnos;

    public ConsultarTurno(TurnoRepository turnos) {
        this.turnos = turnos;
    }

    @Transactional(readOnly = true)
    public TurnoVista ejecutar(Long id) {
        return turnos.findById(id)
                .map(TurnoVista::de)
                .orElseThrow(() -> new ReglaDeNegocioException(CodigoError.RECURSO_INEXISTENTE,
                        "No existe el turno indicado"));
    }
}
