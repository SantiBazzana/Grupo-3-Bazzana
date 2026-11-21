package ar.odonto.turnos.application.configuracion;

import ar.odonto.turnos.domain.error.CodigoError;
import ar.odonto.turnos.domain.error.ReglaDeNegocioException;
import ar.odonto.turnos.domain.model.Sillon;
import ar.odonto.turnos.domain.port.SillonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Edición de sillones (US-002), con baja/reactivación lógica por el mismo PUT (RN-CF-03). */
@Service
public class EditarSillon {

    /** Los campos nulos conservan el valor actual, para admitir ediciones parciales. */
    public record Comando(Long id, String nombre, Boolean activo) {
    }

    private final SillonRepository sillones;

    public EditarSillon(SillonRepository sillones) {
        this.sillones = sillones;
    }

    @Transactional
    public SillonVista ejecutar(Comando comando) {
        Sillon sillon = sillones.findById(comando.id())
                .orElseThrow(() -> new ReglaDeNegocioException(CodigoError.RECURSO_INEXISTENTE,
                        "No existe el sillón indicado"));
        String nombre = comando.nombre() == null ? null : comando.nombre().trim();
        if (nombre != null && !nombre.equals(sillon.getNombre()) && sillones.existsByNombre(nombre)) {
            throw new ReglaDeNegocioException(CodigoError.DATO_DUPLICADO, "Ya existe ese sillón");
        }
        if (nombre != null) {
            sillon.renombrar(nombre);
        }
        if (comando.activo() != null) {
            if (comando.activo()) {
                sillon.activar();
            } else {
                sillon.desactivar();
            }
        }
        return SillonVista.de(sillones.save(sillon));
    }
}
