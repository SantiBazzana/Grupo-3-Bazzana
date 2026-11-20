package ar.odonto.turnos.infrastructure.config;

import ar.odonto.turnos.domain.model.Consultorio;
import ar.odonto.turnos.domain.model.HorarioAtencion;
import ar.odonto.turnos.domain.model.Paciente;
import ar.odonto.turnos.domain.model.Prestacion;
import ar.odonto.turnos.domain.model.Profesional;
import ar.odonto.turnos.domain.model.Rol;
import ar.odonto.turnos.domain.model.Sillon;
import ar.odonto.turnos.domain.model.Usuario;
import ar.odonto.turnos.domain.port.ConsultorioRepository;
import ar.odonto.turnos.domain.port.HorarioAtencionRepository;
import ar.odonto.turnos.domain.port.PacienteRepository;
import ar.odonto.turnos.domain.port.PrestacionRepository;
import ar.odonto.turnos.domain.port.ProfesionalRepository;
import ar.odonto.turnos.domain.port.SillonRepository;
import ar.odonto.turnos.domain.port.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Datos de ejemplo del perfil {@code dev} (spec base-del-sistema, design D9). TODOS FICTICIOS (regla D1):
 * DNI 0000000X, teléfono 549110000000X. Idempotente: solo carga si la base está vacía.
 * Los usuarios se crean solo si su contraseña llega por variable de entorno.
 */
@Component
@Profile("dev")
public class DatosDeEjemploDev implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosDeEjemploDev.class);

    private final ConsultorioRepository consultorios;
    private final HorarioAtencionRepository horarios;
    private final SillonRepository sillones;
    private final ProfesionalRepository profesionales;
    private final PrestacionRepository prestaciones;
    private final PacienteRepository pacientes;
    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final SeedProperties seed;
    private final TurnosProperties turnos;

    public DatosDeEjemploDev(ConsultorioRepository consultorios, HorarioAtencionRepository horarios,
            SillonRepository sillones, ProfesionalRepository profesionales, PrestacionRepository prestaciones,
            PacienteRepository pacientes, UsuarioRepository usuarios, PasswordEncoder passwordEncoder,
            SeedProperties seed, TurnosProperties turnos) {
        this.consultorios = consultorios;
        this.horarios = horarios;
        this.sillones = sillones;
        this.profesionales = profesionales;
        this.prestaciones = prestaciones;
        this.pacientes = pacientes;
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.seed = seed;
        this.turnos = turnos;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (consultorios.count() > 0) {
            log.info("Datos de ejemplo ya cargados; no se vuelven a cargar.");
            return;
        }

        Consultorio consultorio = consultorios.save(new Consultorio("Consultorio Demo", turnos.zonaHoraria()));
        for (DayOfWeek dia : EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)) {
            horarios.save(new HorarioAtencion(consultorio, dia, LocalTime.of(8, 0), LocalTime.of(20, 0)));
        }

        sillones.save(new Sillon("Sillón 1"));
        sillones.save(new Sillon("Sillón 2"));

        Profesional ana = profesionales.save(new Profesional("Ana", "Ejemplo", "MP-0001", "5491100000101"));
        profesionales.save(new Profesional("Bruno", "Prueba", "MP-0002", "5491100000102"));

        prestaciones.save(new Prestacion("Control", 30));
        prestaciones.save(new Prestacion("Limpieza", 45));
        prestaciones.save(new Prestacion("Conducto", 60));
        prestaciones.save(new Prestacion("Extracción", 40));

        pacientes.save(new Paciente("Paciente", "Uno", "00000001", "5491100000001", null));
        pacientes.save(new Paciente("Paciente", "Dos", "00000002", "5491100000002", null));
        pacientes.save(new Paciente("Paciente", "Tres", "00000003", "5491100000003", null));

        cargarUsuarios(ana);
        log.info("Datos de ejemplo ficticios cargados (perfil dev).");
    }

    private void cargarUsuarios(Profesional ana) {
        List<String> faltantes = new ArrayList<>();
        crearSiHayPassword("admin", seed.adminPassword(), Rol.ADMIN, null, "SEED_ADMIN_PASSWORD", faltantes);
        crearSiHayPassword("secretaria", seed.secretariaPassword(), Rol.SECRETARIA, null, "SEED_SECRETARIA_PASSWORD",
                faltantes);
        crearSiHayPassword("ana", seed.odontologoPassword(), Rol.ODONTOLOGO, ana, "SEED_ODONTOLOGO_PASSWORD",
                faltantes);
        if (!faltantes.isEmpty()) {
            // Solo nombres de variables, nunca valores (RN-AU-02).
            log.info("Usuarios de ejemplo deshabilitados: definir {} para habilitarlos.", String.join(", ", faltantes));
        }
    }

    private void crearSiHayPassword(String username, String password, Rol rol, Profesional profesional,
            String variable, List<String> faltantes) {
        if (password == null || password.isBlank()) {
            faltantes.add(variable);
            return;
        }
        usuarios.save(new Usuario(username, passwordEncoder.encode(password), rol, profesional));
    }
}
