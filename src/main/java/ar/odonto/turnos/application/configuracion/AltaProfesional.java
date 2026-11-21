package ar.odonto.turnos.application.configuracion;

import ar.odonto.turnos.domain.error.CodigoError;
import ar.odonto.turnos.domain.error.ReglaDeNegocioException;
import ar.odonto.turnos.domain.model.Profesional;
import ar.odonto.turnos.domain.port.ProfesionalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Alta de profesionales (US-003 acotado: sin vínculo de usuario, decisión c). */
@Service
public class AltaProfesional {

    public record Comando(String nombre, String apellido, String matricula, String telefono) {
    }

    private final ProfesionalRepository profesionales;

    public AltaProfesional(ProfesionalRepository profesionales) {
        this.profesionales = profesionales;
    }

    @Transactional
    public ProfesionalVista ejecutar(Comando comando) {
        String matricula = comando.matricula().trim();
        if (profesionales.existsByMatricula(matricula)) {
            throw new ReglaDeNegocioException(CodigoError.DATO_DUPLICADO, "Ya existe esa matrícula");
        }
        return ProfesionalVista.de(profesionales.save(new Profesional(comando.nombre().trim(),
                comando.apellido().trim(), matricula, comando.telefono())));
    }
}
