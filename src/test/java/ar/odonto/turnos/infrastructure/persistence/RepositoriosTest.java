package ar.odonto.turnos.infrastructure.persistence;

import ar.odonto.turnos.domain.model.Consultorio;
import ar.odonto.turnos.domain.model.HorarioAtencion;
import ar.odonto.turnos.domain.model.Rol;
import ar.odonto.turnos.domain.model.Usuario;
import ar.odonto.turnos.domain.port.ConsultorioRepository;
import ar.odonto.turnos.domain.port.HorarioAtencionRepository;
import ar.odonto.turnos.domain.port.PacienteRepository;
import ar.odonto.turnos.domain.port.PrestacionRepository;
import ar.odonto.turnos.domain.port.ProfesionalRepository;
import ar.odonto.turnos.domain.port.SillonRepository;
import ar.odonto.turnos.domain.port.UsuarioRepository;
import ar.odonto.turnos.support.DatosDePrueba;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.DayOfWeek;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tarea 4.2 de C-01: los puertos se implementan con Spring Data y las restricciones únicas se respetan.
 */
@DataJpaTest
class RepositoriosTest {

    @Autowired ProfesionalRepository profesionales;
    @Autowired SillonRepository sillones;
    @Autowired PrestacionRepository prestaciones;
    @Autowired PacienteRepository pacientes;
    @Autowired UsuarioRepository usuarios;
    @Autowired ConsultorioRepository consultorios;
    @Autowired HorarioAtencionRepository horarios;

    @Test
    void existsBy_detectaLosDatosUnicosYaCargados() {
        profesionales.save(DatosDePrueba.profesional("MP-0001"));
        sillones.save(DatosDePrueba.sillon("Sillón 1"));
        prestaciones.save(DatosDePrueba.prestacion("Control", 30));
        pacientes.save(DatosDePrueba.paciente(1));

        assertThat(profesionales.existsByMatricula("MP-0001")).isTrue();
        assertThat(profesionales.existsByMatricula("MP-9999")).isFalse();
        assertThat(sillones.existsByNombre("Sillón 1")).isTrue();
        assertThat(prestaciones.existsByNombre("Control")).isTrue();
        assertThat(pacientes.existsByDni("00000001")).isTrue();
        assertThat(pacientes.findByDni("00000001")).isPresent();
    }

    // Con IDs IDENTITY, Hibernate inserta en el save: la violación sale ahí, traducida por Spring a
    // DataIntegrityViolationException con causa Hibernate UNIQUE (lo que el manejador mapea a DATO_DUPLICADO).
    @Test
    void matriculaDuplicada_violaLaRestriccionUnica() {
        profesionales.save(DatosDePrueba.profesional("MP-0001"));

        assertThatThrownBy(() -> profesionales.save(DatosDePrueba.profesional("MP-0001")))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasCauseInstanceOf(ConstraintViolationException.class)
                .cause().extracting(causa -> ((ConstraintViolationException) causa).getKind())
                .isEqualTo(ConstraintViolationException.ConstraintKind.UNIQUE);
    }

    @Test
    void dniDuplicado_violaLaRestriccionUnica() {
        pacientes.save(DatosDePrueba.paciente(1));

        assertThatThrownBy(() -> pacientes.save(DatosDePrueba.paciente(1)))
                .isInstanceOf(DataIntegrityViolationException.class)
                .cause().extracting(causa -> ((ConstraintViolationException) causa).getKind())
                .isEqualTo(ConstraintViolationException.ConstraintKind.UNIQUE);
    }

    @Test
    void horario_seBuscaPorConsultorioYDia() {
        Consultorio consultorio = consultorios.save(DatosDePrueba.consultorio());
        HorarioAtencion lunes = horarios.save(DatosDePrueba.horario(consultorio, DayOfWeek.MONDAY));

        assertThat(horarios.findByConsultorioIdAndDiaSemana(consultorio.getId(), DayOfWeek.MONDAY))
                .contains(lunes);
        assertThat(horarios.findByConsultorioIdAndDiaSemana(consultorio.getId(), DayOfWeek.SUNDAY)).isEmpty();
    }

    @Test
    void usuario_seBuscaPorUsername() {
        usuarios.save(new Usuario("secretaria", "$2a$hash-de-prueba", Rol.SECRETARIA, null));

        assertThat(usuarios.existsByUsername("secretaria")).isTrue();
        assertThat(usuarios.findByUsername("secretaria")).get()
                .extracting(Usuario::getRol).isEqualTo(Rol.SECRETARIA);
    }
}
