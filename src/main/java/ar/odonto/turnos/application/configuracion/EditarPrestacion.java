package ar.odonto.turnos.application.configuracion;

import ar.odonto.turnos.domain.error.CodigoError;
import ar.odonto.turnos.domain.error.ReglaDeNegocioException;
import ar.odonto.turnos.domain.model.Prestacion;
import ar.odonto.turnos.domain.port.PrestacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Edición de prestaciones: reutiliza la invariante de duración y evita el falso duplicado (decisión d). */
@Service
public class EditarPrestacion {

    /** Los campos nulos conservan el valor actual, para admitir ediciones parciales. */
    public record Comando(Long id, String nombre, Integer duracionMinutos, Boolean activa) {
    }

    private final PrestacionRepository prestaciones;

    public EditarPrestacion(PrestacionRepository prestaciones) {
        this.prestaciones = prestaciones;
    }

    @Transactional
    public PrestacionVista ejecutar(Comando comando) {
        Prestacion prestacion = prestaciones.findById(comando.id())
                .orElseThrow(() -> new ReglaDeNegocioException(CodigoError.RECURSO_INEXISTENTE,
                        "No existe la prestación indicada"));
        String nombre = comando.nombre() == null ? prestacion.getNombre() : comando.nombre().trim();
        int duracion = comando.duracionMinutos() == null
                ? prestacion.getDuracionMinutos() : comando.duracionMinutos();
        if (!nombre.equals(prestacion.getNombre()) && prestaciones.existsByNombre(nombre)) {
            throw new ReglaDeNegocioException(CodigoError.DATO_DUPLICADO, "Ya existe esa prestación");
        }
        prestacion.actualizar(nombre, duracion);
        if (comando.activa() != null) {
            if (comando.activa()) {
                prestacion.activar();
            } else {
                prestacion.desactivar();
            }
        }
        return PrestacionVista.de(prestaciones.save(prestacion));
    }
}
