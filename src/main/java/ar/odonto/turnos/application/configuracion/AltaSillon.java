package ar.odonto.turnos.application.configuracion;

import ar.odonto.turnos.domain.error.CodigoError;
import ar.odonto.turnos.domain.error.ReglaDeNegocioException;
import ar.odonto.turnos.domain.model.Sillon;
import ar.odonto.turnos.domain.port.SillonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Alta de sillones (US-002). */
@Service
public class AltaSillon {

    public record Comando(String nombre) {
    }

    private final SillonRepository sillones;

    public AltaSillon(SillonRepository sillones) {
        this.sillones = sillones;
    }

    @Transactional
    public SillonVista ejecutar(Comando comando) {
        String nombre = comando.nombre().trim();
        if (sillones.existsByNombre(nombre)) {
            throw new ReglaDeNegocioException(CodigoError.DATO_DUPLICADO, "Ya existe ese sillón");
        }
        return SillonVista.de(sillones.save(new Sillon(nombre)));
    }
}
