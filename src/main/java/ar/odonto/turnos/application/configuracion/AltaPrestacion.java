package ar.odonto.turnos.application.configuracion;

import ar.odonto.turnos.domain.error.CodigoError;
import ar.odonto.turnos.domain.error.ReglaDeNegocioException;
import ar.odonto.turnos.domain.model.Prestacion;
import ar.odonto.turnos.domain.port.PrestacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Alta de prestación: reutiliza la invariante de duración del modelo (RN-CF-01). */
@Service
public class AltaPrestacion {

    public record Comando(String nombre, int duracionMinutos) {
    }

    private final PrestacionRepository prestaciones;

    public AltaPrestacion(PrestacionRepository prestaciones) {
        this.prestaciones = prestaciones;
    }

    @Transactional
    public PrestacionVista ejecutar(Comando comando) {
        String nombre = comando.nombre().trim();
        if (prestaciones.existsByNombre(nombre)) {
            throw new ReglaDeNegocioException(CodigoError.DATO_DUPLICADO, "Ya existe esa prestación");
        }
        return PrestacionVista.de(prestaciones.save(new Prestacion(nombre, comando.duracionMinutos())));
    }
}
