package ar.odonto.turnos.application.configuracion;

import ar.odonto.turnos.domain.port.SillonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Listado de sillones activos e inactivos (RN-CF-03). */
@Service
public class ListarSillones {

    private final SillonRepository sillones;

    public ListarSillones(SillonRepository sillones) {
        this.sillones = sillones;
    }

    @Transactional(readOnly = true)
    public List<SillonVista> ejecutar() {
        return sillones.findAll().stream().map(SillonVista::de).toList();
    }
}
