package ar.odonto.turnos.application.configuracion;

import ar.odonto.turnos.domain.error.CodigoError;
import ar.odonto.turnos.domain.error.ReglaDeNegocioException;
import ar.odonto.turnos.domain.model.Profesional;
import ar.odonto.turnos.domain.port.ProfesionalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Edición de profesionales (US-003). La matrícula es inmutable (decisión d): si se envía distinta se rechaza
 * antes de consultar unicidad; si se envía igual o se omite, no se consulta el puerto.
 */
@Service
public class EditarProfesional {

    /** Los campos nulos conservan el valor actual, para admitir ediciones parciales. */
    public record Comando(Long id, String nombre, String apellido, String matricula, String telefono,
                          Boolean activo) {
    }

    private final ProfesionalRepository profesionales;

    public EditarProfesional(ProfesionalRepository profesionales) {
        this.profesionales = profesionales;
    }

    @Transactional
    public ProfesionalVista ejecutar(Comando comando) {
        Profesional profesional = profesionales.findById(comando.id())
                .orElseThrow(() -> new ReglaDeNegocioException(CodigoError.RECURSO_INEXISTENTE,
                        "No existe el profesional indicado"));
        if (comando.matricula() != null) {
            String matricula = comando.matricula().trim();
            if (!matricula.equals(profesional.getMatricula())) {
                throw new ReglaDeNegocioException(CodigoError.ENTRADA_INVALIDA,
                        "La matrícula es inmutable");
            }
        }
        String nombre = comando.nombre() == null ? profesional.getNombre() : comando.nombre().trim();
        String apellido = comando.apellido() == null ? profesional.getApellido() : comando.apellido().trim();
        String telefono = comando.telefono() == null ? profesional.getTelefono() : comando.telefono();
        profesional.actualizar(nombre, apellido, telefono);
        if (comando.activo() != null) {
            if (comando.activo()) {
                profesional.activar();
            } else {
                profesional.desactivar();
            }
        }
        return ProfesionalVista.de(profesionales.save(profesional));
    }
}
