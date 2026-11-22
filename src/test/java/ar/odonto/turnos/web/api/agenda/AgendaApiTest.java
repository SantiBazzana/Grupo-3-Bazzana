package ar.odonto.turnos.web.api.agenda;

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
import ar.odonto.turnos.domain.port.UsuarioActual;
import ar.odonto.turnos.support.DatosDePrueba;
import ar.odonto.turnos.support.RelojAjustableDePrueba;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.convention.TestBean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tareas 2.1 a 2.3 de C-07: endpoint GET /api/agenda (spec agenda). Contexto de la spec: lunes 2026-11-23,
 * Ana con turnos 09:00, 10:00 (cancelado) y 11:30; zona AR. El solicitante se inyecta mockeado para cubrir
 * los escenarios de visibilidad (RN-AG-02) sin depender de C-05. Cada test revierte sus datos (transacción).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AgendaApiTest {

    static final LocalDateTime AHORA = LocalDateTime.parse("2026-11-23T08:00");
    static final String FECHA = "2026-11-23";

    @TestBean(name = "reloj")
    Clock reloj;

    static Clock reloj() {
        return new RelojAjustableDePrueba(AHORA);
    }

    @MockitoBean
    UsuarioActual usuarioActual;

    @Autowired MockMvc mvc;
    @Autowired ProfesionalRepository profesionales;
    @Autowired SillonRepository sillones;
    @Autowired PacienteRepository pacientes;
    @Autowired PrestacionRepository prestaciones;
    @Autowired TurnoRepository turnos;

    Profesional ana;
    Profesional bruno;
    Sillon sillon1;
    Paciente paciente1;
    Prestacion control;

    @BeforeEach
    void contextoComun() {
        ((RelojAjustableDePrueba) reloj).fijar(AHORA);
        when(usuarioActual.solicitante()).thenReturn(Solicitante.SISTEMA);
        ana = profesionales.save(DatosDePrueba.profesional("MP-0001"));
        bruno = profesionales.save(new Profesional("Bruno", "Prueba", "MP-0002", "5491100000102"));
        sillon1 = sillones.save(DatosDePrueba.sillon("Sillón 1"));
        paciente1 = pacientes.save(new Paciente("Maria", "Lopez", "00000001", "5491100000001", null));
        control = prestaciones.save(DatosDePrueba.prestacion("Control", 30));
    }

    private void como(Solicitante solicitante) {
        when(usuarioActual.solicitante()).thenReturn(solicitante);
    }

    private Turno guardar(String inicio, String fin, Paciente paciente, Sillon sillon, Prestacion prestacion,
            boolean sobreturno) {
        return turnos.save(new Turno(ana, sillon, paciente, prestacion, LocalDateTime.parse(inicio),
                LocalDateTime.parse(fin), sobreturno, LocalDateTime.parse("2026-11-20T10:00"), "prueba"));
    }

    // Tarea 2.1: happy path del endpoint.
    @Test
    void agendaDelDia_feliz_devuelveDosTurnosOrdenadosSinDatosPersonales() throws Exception {
        Turno primero = guardar("2026-11-23T09:00", "2026-11-23T09:30", paciente1, sillon1, control, false);
        Turno cancelado = guardar("2026-11-23T10:00", "2026-11-23T10:30", paciente1, sillon1, control, false);
        cancelado.marcarCancelado(LocalDateTime.parse("2026-11-22T10:00"));
        Turno tercero = guardar("2026-11-23T11:30", "2026-11-23T12:00", paciente1, sillon1, control, true);

        mvc.perform(get("/api/agenda")
                        .param("profesionalId", ana.getId().toString())
                        .param("fecha", FECHA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fecha").value("2026-11-23"))
                .andExpect(jsonPath("$.profesionalId").value(ana.getId()))
                .andExpect(jsonPath("$.turnos.length()").value(2))
                .andExpect(jsonPath("$.turnos[0].id").value(primero.getId()))
                .andExpect(jsonPath("$.turnos[0].inicio").value("2026-11-23T09:00:00"))
                .andExpect(jsonPath("$.turnos[0].fin").value("2026-11-23T09:30:00"))
                .andExpect(jsonPath("$.turnos[0].sobreturno").value(false))
                .andExpect(jsonPath("$.turnos[0].pacienteId").value(paciente1.getId()))
                .andExpect(jsonPath("$.turnos[0].pacienteNombre").value("Maria Lopez"))
                .andExpect(jsonPath("$.turnos[0].prestacionId").value(control.getId()))
                .andExpect(jsonPath("$.turnos[0].prestacionNombre").value("Control"))
                .andExpect(jsonPath("$.turnos[0].sillonId").value(sillon1.getId()))
                .andExpect(jsonPath("$.turnos[0].sillonNombre").value("Sillón 1"))
                .andExpect(jsonPath("$.turnos[1].id").value(tercero.getId()))
                .andExpect(jsonPath("$.turnos[1].inicio").value("2026-11-23T11:30:00"))
                .andExpect(jsonPath("$.turnos[1].sobreturno").value(true))
                .andExpect(jsonPath("$.turnos[0].dni").doesNotExist())
                .andExpect(jsonPath("$.turnos[0].telefono").doesNotExist());
    }

    // Tarea 2.2: visibilidad RN-AG-02 con odontólogo ajeno.
    @Test
    void odontologoPideLaAgendaDeOtro_recibe403ConAccesoDenegado() throws Exception {
        como(new Solicitante("ana", Rol.ODONTOLOGO, ana.getId()));

        mvc.perform(get("/api/agenda")
                        .param("profesionalId", bruno.getId().toString())
                        .param("fecha", FECHA))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));
    }

    // Tarea 2.3: parámetros faltantes o mal formados → 400 con ENTRADA_INVALIDA (manejador global).
    @Test
    void parametrosFaltantesOFormatoInvalido_responden400ConEntradaInvalida() throws Exception {
        mvc.perform(get("/api/agenda")
                        .param("profesionalId", ana.getId().toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("ENTRADA_INVALIDA"));

        mvc.perform(get("/api/agenda")
                        .param("profesionalId", "abc")
                        .param("fecha", FECHA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("ENTRADA_INVALIDA"));

        mvc.perform(get("/api/agenda")
                        .param("profesionalId", ana.getId().toString())
                        .param("fecha", "no-es-una-fecha"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("ENTRADA_INVALIDA"));
    }
}

