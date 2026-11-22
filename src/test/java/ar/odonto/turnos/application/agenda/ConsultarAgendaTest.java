package ar.odonto.turnos.application.agenda;

import ar.odonto.turnos.domain.error.CodigoError;
import ar.odonto.turnos.domain.error.ReglaDeNegocioException;
import ar.odonto.turnos.domain.model.Paciente;
import ar.odonto.turnos.domain.model.Prestacion;
import ar.odonto.turnos.domain.model.Profesional;
import ar.odonto.turnos.domain.model.Rol;
import ar.odonto.turnos.domain.model.Sillon;
import ar.odonto.turnos.domain.model.Solicitante;
import ar.odonto.turnos.domain.model.Turno;
import ar.odonto.turnos.domain.port.PacienteRepository;
import ar.odonto.turnos.domain.port.PrestacionRepository;
import ar.odonto.turnos.domain.port.ProfesionalRepository;
import ar.odonto.turnos.domain.port.SillonRepository;
import ar.odonto.turnos.domain.port.TurnoRepository;
import ar.odonto.turnos.support.DatosDePrueba;
import ar.odonto.turnos.support.RelojAjustableDePrueba;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.convention.TestBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tareas 1.1 a 1.4 de C-07: caso de uso "consultar agenda del día" (spec agenda, RN-AG-01/02, design D1-D4).
 * Contexto de la spec: lunes 2026-11-23, Ana con turnos 09:00, 10:00 (cancelado) y 11:30; zona AR.
 * Cada test corre en una transacción que se revierte al terminar. Datos 100 % ficticios (regla D1).
 */
@SpringBootTest
@Transactional
class ConsultarAgendaTest {

    static final LocalDateTime AHORA = LocalDateTime.parse("2026-11-23T08:00");
    static final LocalDate FECHA = LocalDate.parse("2026-11-23");

    @TestBean(name = "reloj")
    Clock reloj;

    static Clock reloj() {
        return new RelojAjustableDePrueba(AHORA);
    }

    @Autowired ConsultarAgenda consultarAgenda;
    @Autowired ProfesionalRepository profesionales;
    @Autowired SillonRepository sillones;
    @Autowired PacienteRepository pacientes;
    @Autowired PrestacionRepository prestaciones;
    @Autowired TurnoRepository turnos;

    Profesional ana;
    Profesional bruno;
    Sillon sillon1;
    Sillon sillon2;
    Paciente paciente1;
    Paciente paciente2;
    Prestacion control;

    @BeforeEach
    void contextoComun() {
        ((RelojAjustableDePrueba) reloj).fijar(AHORA);
        ana = profesionales.save(DatosDePrueba.profesional("MP-0001"));
        bruno = profesionales.save(new Profesional("Bruno", "Prueba", "MP-0002", "5491100000102"));
        sillon1 = sillones.save(DatosDePrueba.sillon("Sillón 1"));
        sillon2 = sillones.save(DatosDePrueba.sillon("Sillón 2"));
        paciente1 = pacientes.save(new Paciente("Maria", "Lopez", "00000001", "5491100000001", null));
        paciente2 = pacientes.save(new Paciente("Juan", "Perez", "00000002", "5491100000002", null));
        control = prestaciones.save(DatosDePrueba.prestacion("Control", 30));
    }

    private Turno guardar(String inicio, String fin, Paciente paciente, Sillon sillon, Prestacion prestacion,
            boolean sobreturno) {
        return turnos.save(new Turno(ana, sillon, paciente, prestacion, LocalDateTime.parse(inicio),
                LocalDateTime.parse(fin), sobreturno, LocalDateTime.parse("2026-11-20T10:00"), "prueba"));
    }

    private void rechazaAcceso(Solicitante solicitante, Long profesionalId) {
        assertThatThrownBy(() -> consultarAgenda.ejecutar(profesionalId, FECHA, solicitante))
                .isInstanceOfSatisfying(ReglaDeNegocioException.class,
                        e -> assertThat(e.codigo()).isEqualTo(CodigoError.ACCESO_DENEGADO));
    }

    // Tarea 1.1: el caso de uso arma el DTO con los nombres e IDs y sin DNI ni teléfono.
    @Test
    void agendaConUnTurno_devuelveNombresEIdsYNoDatosPersonales() {
        Turno activo = guardar("2026-11-23T09:00", "2026-11-23T09:30", paciente1, sillon1, control, false);

        List<TurnoDeAgendaVista> agenda = consultarAgenda.ejecutar(ana.getId(), FECHA, Solicitante.SISTEMA);

        assertThat(agenda).hasSize(1);
        TurnoDeAgendaVista vista = agenda.get(0);
        assertThat(vista.id()).isEqualTo(activo.getId());
        assertThat(vista.inicio()).isEqualTo(LocalDateTime.parse("2026-11-23T09:00"));
        assertThat(vista.fin()).isEqualTo(LocalDateTime.parse("2026-11-23T09:30"));
        assertThat(vista.sobreturno()).isFalse();
        assertThat(vista.profesionalId()).isEqualTo(ana.getId());
        assertThat(vista.pacienteId()).isEqualTo(paciente1.getId());
        assertThat(vista.pacienteNombre()).isEqualTo("Maria Lopez");
        assertThat(vista.prestacionId()).isEqualTo(control.getId());
        assertThat(vista.prestacionNombre()).isEqualTo("Control");
        assertThat(vista.sillonId()).isEqualTo(sillon1.getId());
        assertThat(vista.sillonNombre()).isEqualTo("Sillón 1");
    }

    // Tarea 1.2: límite inferior inclusivo y límite superior exclusivo del día.
    @Test
    void turnoDeLas2359DelDiaAnterior_quedEnFueraDeLaAgenda() {
        guardar("2026-11-22T23:59", "2026-11-23T00:29", paciente1, sillon1, control, false);

        List<TurnoDeAgendaVista> agenda = consultarAgenda.ejecutar(ana.getId(), FECHA, Solicitante.SISTEMA);

        assertThat(agenda).isEmpty();
    }

    @Test
    void turnoDeLas0000DelDiaSiguiente_quedEnFueraDeLaAgenda() {
        guardar("2026-11-24T00:00", "2026-11-24T00:30", paciente1, sillon1, control, false);

        List<TurnoDeAgendaVista> agenda = consultarAgenda.ejecutar(ana.getId(), FECHA, Solicitante.SISTEMA);

        assertThat(agenda).isEmpty();
    }

    @Test
    void turnoDeLas0000DelDiaConsultado_entraEnLaAgenda() {
        Turno activo = guardar("2026-11-23T00:00", "2026-11-23T00:30", paciente1, sillon1, control, false);

        List<TurnoDeAgendaVista> agenda = consultarAgenda.ejecutar(ana.getId(), FECHA, Solicitante.SISTEMA);

        assertThat(agenda).hasSize(1);
        assertThat(agenda.get(0).id()).isEqualTo(activo.getId());
    }

    @Test
    void diaSinTurnos_devuelveListaVacia() {
        List<TurnoDeAgendaVista> agenda = consultarAgenda.ejecutar(ana.getId(), LocalDate.parse("2026-11-26"),
                Solicitante.SISTEMA);

        assertThat(agenda).isEmpty();
    }

    // Tarea 1.3: escenario feliz con un turno CANCELADO y un sobreturno.
    @Test
    void agendaDelDiaConTresTurnosUnoCancelado_devuelveDosOrdenadosPorInicio() {
        Turno primero = guardar("2026-11-23T09:00", "2026-11-23T09:30", paciente1, sillon1, control, false);
        Turno cancelado = guardar("2026-11-23T10:00", "2026-11-23T10:30", paciente2, sillon2, control, false);
        cancelado.marcarCancelado(LocalDateTime.parse("2026-11-22T10:00"));
        Turno tercero = guardar("2026-11-23T11:30", "2026-11-23T12:00", paciente1, sillon1, control, true);

        List<TurnoDeAgendaVista> agenda = consultarAgenda.ejecutar(ana.getId(), FECHA, Solicitante.SISTEMA);

        assertThat(agenda).hasSize(2);
        assertThat(agenda.get(0).id()).isEqualTo(primero.getId());
        assertThat(agenda.get(0).inicio()).isEqualTo(LocalDateTime.parse("2026-11-23T09:00"));
        assertThat(agenda.get(0).sobreturno()).isFalse();
        assertThat(agenda.get(1).id()).isEqualTo(tercero.getId());
        assertThat(agenda.get(1).inicio()).isEqualTo(LocalDateTime.parse("2026-11-23T11:30"));
        assertThat(agenda.get(1).sobreturno()).isTrue();
    }

    // Tarea 1.4: RN-AG-02 antes de consultar la base.
    @Test
    void odontologoPideLaAgendaDeOtro_profesional_lanzaAccesoDenegado() {
        rechazaAcceso(new Solicitante("ana", Rol.ODONTOLOGO, ana.getId()), bruno.getId());
    }

    @Test
    void odontologoSinProfesionalVinculado_lanzaAccesoDenegado() {
        rechazaAcceso(new Solicitante("pedro", Rol.ODONTOLOGO, null), ana.getId());
    }

    @Test
    void odontologoPideSuPropiaAgenda_laAcepta() {
        Turno activo = guardar("2026-11-23T09:00", "2026-11-23T09:30", paciente1, sillon1, control, false);
        List<TurnoDeAgendaVista> agenda = consultarAgenda.ejecutar(ana.getId(), FECHA,
                new Solicitante("ana", Rol.ODONTOLOGO, ana.getId()));

        assertThat(agenda).hasSize(1);
        assertThat(agenda.get(0).id()).isEqualTo(activo.getId());
    }

    @Test
    void secretariaPuedePedirLaAgendaDeCualquierProfesional() {
        List<TurnoDeAgendaVista> agenda = consultarAgenda.ejecutar(bruno.getId(), FECHA,
                new Solicitante("secretaria", Rol.SECRETARIA, null));

        assertThat(agenda).isEmpty();
    }

    @Test
    void adminPuedePedirLaAgendaDeCualquierProfesional() {
        List<TurnoDeAgendaVista> agenda = consultarAgenda.ejecutar(bruno.getId(), FECHA, Solicitante.SISTEMA);

        assertThat(agenda).isEmpty();
    }
}
