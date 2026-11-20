package ar.odonto.turnos.application.turnos;

import ar.odonto.turnos.domain.error.CodigoError;
import ar.odonto.turnos.domain.error.ReglaDeNegocioException;
import ar.odonto.turnos.domain.model.Consultorio;
import ar.odonto.turnos.domain.model.Paciente;
import ar.odonto.turnos.domain.model.Prestacion;
import ar.odonto.turnos.domain.model.Profesional;
import ar.odonto.turnos.domain.model.Sillon;
import ar.odonto.turnos.domain.port.ConsultorioRepository;
import ar.odonto.turnos.domain.port.HorarioAtencionRepository;
import ar.odonto.turnos.domain.port.PacienteRepository;
import ar.odonto.turnos.domain.port.PrestacionRepository;
import ar.odonto.turnos.domain.port.ProfesionalRepository;
import ar.odonto.turnos.domain.port.SillonRepository;
import ar.odonto.turnos.domain.port.TurnoRepository;
import ar.odonto.turnos.support.DatosDePrueba;
import ar.odonto.turnos.support.RelojDePrueba;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.convention.TestBean;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Spec turnos, requisito "Reservas simultáneas" (RN-TU-10, design D4/D6 de C-02).
 * SIN @Transactional: cada hilo usa su propia transacción y los datos se confirman de verdad.
 * Contexto y base aislados (propiedad propia) y @DirtiesContext para no afectar a otros tests.
 */
@SpringBootTest(properties = "turnos.test.contexto=concurrencia")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CrearTurnoConcurrenteTest {

    @TestBean(name = "reloj")
    Clock reloj;

    static Clock reloj() {
        return RelojDePrueba.en("2026-11-23T08:00");
    }

    @Autowired CrearTurno crearTurno;
    @Autowired ConsultorioRepository consultorios;
    @Autowired HorarioAtencionRepository horarios;
    @Autowired ProfesionalRepository profesionales;
    @Autowired SillonRepository sillones;
    @Autowired PacienteRepository pacientes;
    @Autowired PrestacionRepository prestaciones;
    @Autowired TurnoRepository turnos;

    Profesional ana;
    Sillon sillon1;
    Paciente paciente1;
    Paciente paciente2;
    Prestacion control;

    @BeforeAll
    void datos() {
        Consultorio consultorio = consultorios.save(DatosDePrueba.consultorio());
        for (DayOfWeek dia : EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)) {
            horarios.save(DatosDePrueba.horario(consultorio, dia));
        }
        ana = profesionales.save(DatosDePrueba.profesional("MP-0001"));
        sillon1 = sillones.save(DatosDePrueba.sillon("Sillón 1"));
        paciente1 = pacientes.save(DatosDePrueba.paciente(1));
        paciente2 = pacientes.save(DatosDePrueba.paciente(2));
        control = prestaciones.save(DatosDePrueba.prestacion("Control", 30));
    }

    @RepeatedTest(3)
    void dosSecretariasReservanElMismoHueco(RepetitionInfo repeticion) throws Exception {
        // Cada repetición usa un horario distinto (11:00, 12:00, 13:00) porque los turnos anteriores quedan guardados.
        LocalDateTime inicio = LocalDateTime.parse("2026-11-23T10:00").plusHours(repeticion.getCurrentRepetition());
        long antes = turnos.count();

        CountDownLatch largada = new CountDownLatch(1);
        ExecutorService hilos = Executors.newFixedThreadPool(2);
        try {
            List<Future<String>> resultados = new ArrayList<>();
            for (Paciente paciente : List.of(paciente1, paciente2)) {
                Callable<String> reserva = () -> {
                    largada.await();
                    try {
                        crearTurno.ejecutar(new CrearTurno.Comando(paciente.getId(), ana.getId(), sillon1.getId(),
                                control.getId(), inicio, false));
                        return "CREADO";
                    } catch (ReglaDeNegocioException e) {
                        return e.codigo().name();
                    }
                };
                resultados.add(hilos.submit(reserva));
            }
            largada.countDown(); // las dos reservas arrancan a la vez

            List<String> codigos = new ArrayList<>();
            for (Future<String> r : resultados) {
                codigos.add(r.get(20, TimeUnit.SECONDS));
            }

            assertThat(codigos).containsExactlyInAnyOrder("CREADO", CodigoError.CHOQUE_SILLON.name());
            assertThat(turnos.count()).isEqualTo(antes + 1);
            assertThat(turnos.buscarChoquesDeSillon(sillon1.getId(), inicio, inicio.plusMinutes(30), null)).hasSize(1);
        } finally {
            hilos.shutdownNow();
        }
    }
}
