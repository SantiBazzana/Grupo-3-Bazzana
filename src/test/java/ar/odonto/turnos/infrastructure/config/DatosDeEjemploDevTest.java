package ar.odonto.turnos.infrastructure.config;

import ar.odonto.turnos.domain.model.Paciente;
import ar.odonto.turnos.domain.model.Prestacion;
import ar.odonto.turnos.domain.model.Rol;
import ar.odonto.turnos.domain.model.Usuario;
import ar.odonto.turnos.domain.port.ConsultorioRepository;
import ar.odonto.turnos.domain.port.HorarioAtencionRepository;
import ar.odonto.turnos.domain.port.PacienteRepository;
import ar.odonto.turnos.domain.port.PrestacionRepository;
import ar.odonto.turnos.domain.port.ProfesionalRepository;
import ar.odonto.turnos.domain.port.SillonRepository;
import ar.odonto.turnos.domain.port.UsuarioRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Spec base-del-sistema: "Datos de ejemplo ficticios solo en desarrollo" y
 * "Usuarios de ejemplo sin contraseñas versionadas". Cada contexto usa su propia base en memoria.
 */
class DatosDeEjemploDevTest {

    // Contraseñas de PRUEBA, solo existen dentro de este test (no son credenciales reales).
    private static final String PASS_ADMIN = "prueba-admin-123";
    private static final String PASS_SECRETARIA = "prueba-secretaria-123";
    private static final String PASS_ODONTOLOGO = "prueba-odontologo-123";

    @Nested
    @ExtendWith(OutputCaptureExtension.class)
    @SpringBootTest(properties = {
            "spring.datasource.url=jdbc:h2:mem:seed-con-usuarios;DB_CLOSE_DELAY=-1",
            "turnos.seed.admin-password=" + PASS_ADMIN,
            "turnos.seed.secretaria-password=" + PASS_SECRETARIA,
            "turnos.seed.odontologo-password=" + PASS_ODONTOLOGO})
    @ActiveProfiles("dev")
    @Transactional
    class ConContrasenasProvistas {

        @Autowired ConsultorioRepository consultorios;
        @Autowired HorarioAtencionRepository horarios;
        @Autowired SillonRepository sillones;
        @Autowired ProfesionalRepository profesionales;
        @Autowired PrestacionRepository prestaciones;
        @Autowired PacienteRepository pacientes;
        @Autowired UsuarioRepository usuarios;
        @Autowired PasswordEncoder passwordEncoder;

        @Test
        void cargaConsultorioHorarioSillonesProfesionalesPrestacionesYPacientes() {
            assertThat(consultorios.count()).isEqualTo(1);
            assertThat(horarios.findAll()).hasSize(5).allSatisfy(h -> {
                assertThat(h.getDiaSemana()).isBetween(DayOfWeek.MONDAY, DayOfWeek.FRIDAY);
                assertThat(h.getApertura()).isEqualTo(LocalTime.of(8, 0));
                assertThat(h.getCierre()).isEqualTo(LocalTime.of(20, 0));
            });
            assertThat(sillones.count()).isEqualTo(2);
            assertThat(profesionales.count()).isEqualTo(2);
            Map<String, Integer> duraciones = prestaciones.findAll().stream()
                    .collect(Collectors.toMap(Prestacion::getNombre, Prestacion::getDuracionMinutos));
            assertThat(duraciones).containsExactlyInAnyOrderEntriesOf(
                    Map.of("Control", 30, "Limpieza", 45, "Conducto", 60, "Extracción", 40));
            assertThat(pacientes.count()).isEqualTo(3);
        }

        @Test
        void losPacientesSonFicticios() {
            assertThat(pacientes.findAll()).allSatisfy((Paciente p) -> {
                assertThat(p.getDni()).matches("0000000\\d");
                assertThat(p.getTelefono()).matches("549110000000\\d");
            });
        }

        @Test
        void creaLosTresUsuariosConContrasenaHasheada() {
            assertThat(usuarios.count()).isEqualTo(3);
            Usuario admin = usuarios.findByUsername("admin").orElseThrow();
            Usuario ana = usuarios.findByUsername("ana").orElseThrow();

            assertThat(admin.getRol()).isEqualTo(Rol.ADMIN);
            assertThat(usuarios.findByUsername("secretaria").orElseThrow().getRol()).isEqualTo(Rol.SECRETARIA);
            assertThat(ana.getRol()).isEqualTo(Rol.ODONTOLOGO);
            assertThat(ana.getProfesional()).isNotNull();

            assertThat(admin.getPasswordHash()).isNotEqualTo(PASS_ADMIN);
            assertThat(passwordEncoder.matches(PASS_ADMIN, admin.getPasswordHash())).isTrue();
        }

        @Test
        void ningunaContrasenaApareceEnElLog(CapturedOutput salida) {
            assertThat(salida.getAll()).contains("Datos de ejemplo ficticios cargados")
                    .doesNotContain(PASS_ADMIN, PASS_SECRETARIA, PASS_ODONTOLOGO);
        }
    }

    @Nested
    @ExtendWith(OutputCaptureExtension.class)
    @SpringBootTest(properties = {
            "spring.datasource.url=jdbc:h2:mem:seed-sin-usuarios;DB_CLOSE_DELAY=-1",
            "turnos.seed.admin-password=",
            "turnos.seed.secretaria-password=",
            "turnos.seed.odontologo-password="})
    @ActiveProfiles("dev")
    class SinContrasenas {

        @Autowired UsuarioRepository usuarios;
        @Autowired PacienteRepository pacientes;

        @Test
        void noCreaUsuarios_peroSiElRestoDeLosDatos() {
            assertThat(usuarios.count()).isZero();
            assertThat(pacientes.count()).isEqualTo(3);
        }

        @Test
        void elLogIndicaComoHabilitarlos(CapturedOutput salida) {
            // OutputCaptureExtension captura desde beforeAll, antes de que Spring arranque este contexto (que es
            // nuevo porque sus propiedades son distintas), así que el log del seed queda capturado.
            assertThat(salida.getAll())
                    .contains("Usuarios de ejemplo deshabilitados")
                    .contains("SEED_ADMIN_PASSWORD", "SEED_SECRETARIA_PASSWORD", "SEED_ODONTOLOGO_PASSWORD");
        }
    }
}
