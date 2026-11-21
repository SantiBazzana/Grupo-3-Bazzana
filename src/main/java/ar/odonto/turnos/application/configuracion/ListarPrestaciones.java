package ar.odonto.turnos.application.configuracion;

import ar.odonto.turnos.domain.port.PrestacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Listado de prestaciones activas e inactivas (RN-CF-03). */
@Service
public class ListarPrestaciones {

    private final PrestacionRepository prestaciones;

    public ListarPrestaciones(PrestacionRepository prestaciones) {
        this.prestaciones = prestaciones;
    }

    @Transactional(readOnly = true)
    public List<PrestacionVista> ejecutar() {
        return prestaciones.findAll().stream().map(PrestacionVista::de).toList();
    }
}
