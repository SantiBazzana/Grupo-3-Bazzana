package ar.odonto.turnos.web.api.turnos;

import ar.odonto.turnos.domain.model.Consultorio;
import ar.odonto.turnos.domain.model.Paciente;
import ar.odonto.turnos.domain.model.Prestacion;
import ar.odonto.turnos.domain.model.Profesional;
import ar.odonto.turnos.domain.model.Sillon;
import ar.odonto.turnos.domain.model.Turno;
import ar.odonto.turnos.domain.port.ConsultorioRepository;
import ar.odonto.turnos.domain.port.HorarioAtencionRepository;
import ar.odonto.turnos.domain.port.PacienteRepository;
import ar.odonto.turnos.domain.port.PrestacionRepository;
import ar.odonto.turnos.domain.port.ProfesionalRepository;
import ar.odonto.turnos.domain.port.SillonRepository;
import ar.odonto.turnos.domain.port.TurnoRepository;
import ar.odonto.turnos.support.DatosDePrueba;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.convention.TestBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Un test por escenario de la spec "turnos" de C-02 (salvo concurrencia, en CrearTurnoConcurrenteTest).
 * Contexto común: L-V 08:00-20:00, ahora = lunes 2026-11-23 08:00, Ana y Bruno, Sillón 1 y 2, Control 30 min,
 * margen 5 min. "Existe el turno 09:30-10:00" = turno RESERVADO de Ana, Sillón 1, Paciente Uno.
 * Cada test corre en una transacción que se revierte al terminar. Datos 100 % ficticios (regla D1).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TurnosApiTest {

    static final LocalDateTime AHORA = LocalDateTime.parse("2026-11-23T08:00");

    @TestBean(name = "reloj")
    Clock reloj;

    static Clock reloj() {
        return new RelojAjustable(AHORA);
    }

    @Autowired MockMvc mvc;
    @Autowired ConsultorioRepository consultorios;
    @Autowired HorarioAtencionRepository horarios;
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
        ((RelojAjustable) reloj).fijar(AHORA);
        Consultorio consultorio = consultorios.save(DatosDePrueba.consultorio());
        for (DayOfWeek dia : EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)) {
            horarios.save(DatosDePrueba.horario(consultorio, dia));
        }
        ana = profesionales.save(DatosDePrueba.profesional("MP-0001"));
        bruno = profesionales.save(new Profesional("Bruno", "Prueba", "MP-0002", "5491100000102"));
        sillon1 = sillones.save(DatosDePrueba.sillon("Sillón 1"));
        sillon2 = sillones.save(DatosDePrueba.sillon("Sillón 2"));
        paciente1 = pacientes.save(DatosDePrueba.paciente(1));
        paciente2 = pacientes.save(DatosDePrueba.paciente(2));
        control = prestaciones.save(DatosDePrueba.prestacion("Control", 30));
    }

    Turno existeTurno0930() {
        return turnos.save(DatosDePrueba.turno(ana, sillon1, paciente1, control, "2026-11-23T09:30", "2026-11-23T10:00"));
    }

    ResultActions crear(Profesional prof, Sillon sillon, Paciente paciente, String inicio, boolean sobreturno)
            throws Exception {
        String json = """
                {"pacienteId": %d, "profesionalId": %d, "sillonId": %s, "prestacionId": %d,
                 "inicio": "%s", "sobreturno": %b}""".formatted(paciente.getId(), prof.getId(),
                sillon == null ? "null" : sillon.getId().toString(), control.getId(), inicio, sobreturno);
        return mvc.perform(post("/api/turnos").contentType(MediaType.APPLICATION_JSON).content(json));
    }

    ResultActions crear(Profesional prof, Sillon sillon, Paciente paciente, String inicio) throws Exception {
        return crear(prof, sillon, paciente, inicio, false);
    }

    static void codigo(ResultActions r, int estado, String codigo) throws Exception {
        r.andExpect(status().is(estado)).andExpect(jsonPath("$.codigo").value(codigo));
    }

    @Nested
    class CrearConLaDuracionDeLaPrestacion {

        @Test
        void turnoLibreDeChoques_feliz() throws Exception {
            ResultActions respuesta = crear(ana, sillon1, paciente2, "2026-11-23T10:00");
            String idCreado = respuesta.andReturn().getResponse().getContentAsString()
                    .replaceAll("(?s).*\"id\":(\\d+).*", "$1");
            String cuerpo = respuesta
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", endsWith("/api/turnos/" + idCreado)))
                    .andExpect(jsonPath("$.estado").value("RESERVADO"))
                    .andExpect(jsonPath("$.inicio").value("2026-11-23T10:00:00"))
                    .andExpect(jsonPath("$.fin").value("2026-11-23T10:30:00"))
                    .andExpect(jsonPath("$.creadoPor").value("sistema"))
                    .andExpect(jsonPath("$.creadoEn").value("2026-11-23T08:00:00"))
                    .andReturn().getResponse().getContentAsString();
            assertThat(cuerpo).contains("\"id\":" + idCreado);
            mvc.perform(get("/api/turnos/" + idCreado)).andExpect(status().isOk());
            assertThat(turnos.count()).isEqualTo(1);
        }

        @Test
        void elFinEnviadoPorElClienteSeIgnora() throws Exception {
            String json = """
                    {"pacienteId": %d, "profesionalId": %d, "sillonId": %d, "prestacionId": %d,
                     "inicio": "2026-11-23T10:00", "fin": "2026-11-23T12:00"}"""
                    .formatted(paciente2.getId(), ana.getId(), sillon1.getId(), control.getId());
            mvc.perform(post("/api/turnos").contentType(MediaType.APPLICATION_JSON).content(json))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.fin").value("2026-11-23T10:30:00"));
        }
    }

    @Nested
    class SillonExclusivoConMargen {

        @Test
        void sillonOcupado_errorDeNegocio() throws Exception {
            existeTurno0930();
            codigo(crear(bruno, sillon1, paciente2, "2026-11-23T09:45"), 409, "CHOQUE_SILLON");
            assertThat(turnos.count()).isEqualTo(1);
        }

        @Test
        void empieza5MinutosDespues_bordeAceptado() throws Exception {
            existeTurno0930();
            crear(bruno, sillon1, paciente2, "2026-11-23T10:05").andExpect(status().isCreated());
        }

        @Test
        void empiezaJustoCuandoTerminaOtro_bordeRechazado() throws Exception {
            existeTurno0930();
            codigo(crear(bruno, sillon1, paciente2, "2026-11-23T10:00"), 409, "CHOQUE_SILLON");
        }

        @Test
        void terminaJusto5MinutosAntes_bordeAceptado() throws Exception {
            existeTurno0930();
            crear(bruno, sillon1, paciente2, "2026-11-23T08:55").andExpect(status().isCreated());
        }

        @Test
        void unSobreturnoNoPuedeCompartirElSillon() throws Exception {
            existeTurno0930();
            codigo(crear(bruno, sillon1, paciente2, "2026-11-23T09:45", true), 409, "CHOQUE_SILLON");
        }
    }

    @Nested
    class ProfesionalSinChoquesSalvoSobreturno {

        @Test
        void profesionalOcupado() throws Exception {
            existeTurno0930();
            codigo(crear(ana, sillon2, paciente2, "2026-11-23T09:45"), 409, "CHOQUE_PROFESIONAL");
        }

        @Test
        void sobreturnoDelMismoProfesionalEnOtroSillon() throws Exception {
            existeTurno0930();
            crear(ana, sillon2, paciente2, "2026-11-23T09:45", true)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.sobreturno").value(true));
        }

        @Test
        void elSobreturnoNoRespetaMargenConSuProfesional() throws Exception {
            existeTurno0930();
            crear(ana, sillon2, paciente2, "2026-11-23T10:00", true).andExpect(status().isCreated());
        }
    }

    @Nested
    class PacienteSinSuperposicion {

        @Test
        void pacienteConTurnoSuperpuesto() throws Exception {
            existeTurno0930();
            codigo(crear(bruno, sillon2, paciente1, "2026-11-23T09:45"), 409, "CHOQUE_PACIENTE");
        }

        @Test
        void elPacientePuedeEncadenarTurnosSinMargen() throws Exception {
            existeTurno0930();
            crear(bruno, sillon2, paciente1, "2026-11-23T10:00").andExpect(status().isCreated());
        }
    }

    @Nested
    class InicioEnMultiplosDe5 {

        @Test
        void minutoNoMultiploDe5() throws Exception {
            codigo(crear(ana, sillon1, paciente2, "2026-11-23T09:03"), 400, "ENTRADA_INVALIDA");
        }

        @Test
        void segundosDistintosDeCero() throws Exception {
            codigo(crear(ana, sillon1, paciente2, "2026-11-23T09:05:30"), 400, "ENTRADA_INVALIDA");
        }
    }

    @Nested
    class NoEnElPasado {

        @Test
        void turnoEnElPasado() throws Exception {
            codigo(crear(ana, sillon1, paciente2, "2026-11-20T10:00"), 422, "TURNO_EN_PASADO");
        }

        @Test
        void turnoExactamenteEnElAhora() throws Exception {
            codigo(crear(ana, sillon1, paciente2, "2026-11-23T08:00"), 422, "TURNO_EN_PASADO");
        }
    }

    @Nested
    class DentroDelHorario {

        @Test
        void terminaDespuesDelCierre() throws Exception {
            codigo(crear(ana, sillon1, paciente2, "2026-11-23T19:45"), 422, "FUERA_DE_HORARIO");
        }

        @Test
        void terminaJustoAlCierre() throws Exception {
            crear(ana, sillon1, paciente2, "2026-11-23T19:30").andExpect(status().isCreated());
        }

        @Test
        void empiezaAntesDeLaApertura() throws Exception {
            codigo(crear(ana, sillon1, paciente2, "2026-11-24T07:45"), 422, "FUERA_DE_HORARIO");
        }

        @Test
        void diaSinAtencion() throws Exception {
            codigo(crear(ana, sillon1, paciente2, "2026-11-28T10:00"), 422, "FUERA_DE_HORARIO");
        }
    }

    @Nested
    class RecursosExistentesYActivos {

        @Test
        void profesionalInexistente() throws Exception {
            String json = """
                    {"pacienteId": %d, "profesionalId": 999999, "sillonId": %d, "prestacionId": %d,
                     "inicio": "2026-11-23T10:00"}""".formatted(paciente2.getId(), sillon1.getId(), control.getId());
            codigo(mvc.perform(post("/api/turnos").contentType(MediaType.APPLICATION_JSON).content(json)),
                    404, "RECURSO_INEXISTENTE");
        }

        @Test
        void sillonInactivo() throws Exception {
            sillon2.desactivar();
            sillones.save(sillon2);
            codigo(crear(ana, sillon2, paciente2, "2026-11-23T10:00"), 422, "RECURSO_INACTIVO");
        }

        @Test
        void sinSillon() throws Exception {
            codigo(crear(ana, null, paciente2, "2026-11-23T10:00"), 422, "SILLON_REQUERIDO");
        }
    }

    @Nested
    class LosCanceladosNoBloquean {

        @Test
        void horarioDeUnTurnoCancelado() throws Exception {
            Turno existente = existeTurno0930();
            existente.marcarCancelado(LocalDateTime.parse("2026-11-22T10:00"));
            turnos.save(existente);
            crear(ana, sillon1, paciente1, "2026-11-23T09:30").andExpect(status().isCreated());
        }
    }

    @Nested
    class OrdenDeValidacion {

        @Test
        void pasadoYSillonOcupadoALaVez() throws Exception {
            ((RelojAjustable) reloj).fijar(LocalDateTime.parse("2026-11-23T11:00"));
            existeTurno0930();
            codigo(crear(bruno, sillon1, paciente2, "2026-11-23T09:45"), 422, "TURNO_EN_PASADO");
        }

        @Test
        void sillonYProfesionalOcupadosALaVez() throws Exception {
            existeTurno0930();
            codigo(crear(ana, sillon1, paciente2, "2026-11-23T09:45"), 409, "CHOQUE_SILLON");
        }
    }

    @Nested
    class ConsultarUnTurno {

        @Test
        void turnoExistente_sinDniNiTelefono() throws Exception {
            Turno existente = existeTurno0930();
            mvc.perform(get("/api/turnos/" + existente.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.inicio").value("2026-11-23T09:30:00"))
                    .andExpect(jsonPath("$.fin").value("2026-11-23T10:00:00"))
                    .andExpect(jsonPath("$.estado").value("RESERVADO"))
                    .andExpect(jsonPath("$.pacienteId").value(paciente1.getId()))
                    .andExpect(jsonPath("$.profesionalId").value(ana.getId()))
                    .andExpect(jsonPath("$.sillonId").value(sillon1.getId()))
                    .andExpect(jsonPath("$.prestacionId").value(control.getId()))
                    .andExpect(jsonPath("$.dni").doesNotExist())
                    .andExpect(jsonPath("$.telefono").doesNotExist());
        }

        @Test
        void turnoInexistente() throws Exception {
            codigo(mvc.perform(get("/api/turnos/999999")), 404, "RECURSO_INEXISTENTE");
        }
    }
}
