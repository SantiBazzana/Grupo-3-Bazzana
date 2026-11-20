package ar.odonto.turnos.infrastructure.persistence;

import ar.odonto.turnos.domain.port.ConsultorioRepository;
import ar.odonto.turnos.domain.port.HorarioAtencionRepository;
import ar.odonto.turnos.domain.port.PacienteRepository;
import ar.odonto.turnos.domain.port.PrestacionRepository;
import ar.odonto.turnos.domain.port.ProfesionalRepository;
import ar.odonto.turnos.domain.port.SillonRepository;
import ar.odonto.turnos.domain.port.TurnoRepository;
import ar.odonto.turnos.domain.port.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Spec base-del-sistema, "Datos de ejemplo ficticios solo en desarrollo", escenario "Arranque sin perfil dev".
 */
@SpringBootTest
class BaseVaciaSinPerfilDevTest {

    @Autowired ConsultorioRepository consultorios;
    @Autowired HorarioAtencionRepository horarios;
    @Autowired ProfesionalRepository profesionales;
    @Autowired SillonRepository sillones;
    @Autowired PrestacionRepository prestaciones;
    @Autowired PacienteRepository pacientes;
    @Autowired UsuarioRepository usuarios;
    @Autowired TurnoRepository turnos;

    @Test
    void sinPerfilDev_noHayNingunDatoPrecargado() {
        assertThat(consultorios.count()).isZero();
        assertThat(horarios.count()).isZero();
        assertThat(profesionales.count()).isZero();
        assertThat(sillones.count()).isZero();
        assertThat(prestaciones.count()).isZero();
        assertThat(pacientes.count()).isZero();
        assertThat(usuarios.count()).isZero();
        assertThat(turnos.count()).isZero();
    }
}
