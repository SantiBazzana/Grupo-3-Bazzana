package ar.odonto.turnos.application.configuracion;

import ar.odonto.turnos.domain.port.ProfesionalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Listado de profesionales activos e inactivos (RN-CF-03). */
@Service
public class ListarProfesionales {

    private final ProfesionalRepository profesionales;

    public ListarProfesionales(ProfesionalRepository profesionales) {
        this.profesionales = profesionales;
    }

    @Transactional(readOnly = true)
    public List<ProfesionalVista> ejecutar() {
        return profesionales.findAll().stream().map(ProfesionalVista::de).toList();
    }
}
