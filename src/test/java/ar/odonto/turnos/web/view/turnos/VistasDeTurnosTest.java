package ar.odonto.turnos.web.view.turnos;

import ar.odonto.turnos.domain.model.Consultorio;
import ar.odonto.turnos.domain.model.Paciente;
import ar.odonto.turnos.domain.model.Prestacion;
import ar.odonto.turnos.domain.model.Profesional;
import ar.odonto.turnos.domain.model.Rol;
import ar.odonto.turnos.domain.model.Sillon;
import ar.odonto.turnos.domain.model.Solicitante;
import ar.odonto.turnos.domain.model.Turno;
import ar.odonto.turnos.domain.model.TurnoEstado;
import ar.odonto.turnos.domain.port.ConsultorioRepository;
import ar.odonto.turnos.domain.port.HorarioAtencionRepository;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.convention.TestBean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Un test por escenario de la spec `interfaz-turnos` (C-10). Contexto común: L-V 08:00-20:00, reloj movible
 * en zona America/Argentina/Buenos_Aires, Ana/Bruno, Sillón 1/2, Paciente Uno/Dos/SinTeléfono, Control 30 min,
 * margen 5 min y anticipación de cancelación 6 h. Datos 100 % ficticios (D1); nunca `LocalDateTime.now()` (D4).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "secretaria", roles = "SECRETARIA")
class VistasDeTurnosTest {

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
    Paciente sinTelefono;
    Prestacion control;

    @BeforeEach
    void contextoComun() {
        fijar(AHORA);
        como(new Solicitante("secretaria", Rol.SECRETARIA, null));
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
        sinTelefono = pacientes.save(new Paciente("Paciente", "Numero 4", "00000004", null, null));
        control = prestaciones.save(DatosDePrueba.prestacion("Control", 30));
    }

    void como(Solicitante solicitante) {
        when(usuarioActual.solicitante()).thenReturn(solicitante);
    }

    void fijar(LocalDateTime fechaHora) {
        ((RelojAjustableDePrueba) reloj).fijar(fechaHora);
    }

    Turno guardarTurno(Profesional profesional, Sillon sillon, Paciente paciente, String inicio, String fin,
            boolean sobreturno) {
        return turnos.save(new Turno(profesional, sillon, paciente, control, LocalDateTime.parse(inicio),
                LocalDateTime.parse(fin), sobreturno, LocalDateTime.parse("2026-11-20T10:00"), "secretaria"));
    }

    MockHttpServletRequestBuilder postConCsrf(String ruta, String... parametros) {
        MockHttpServletRequestBuilder peticion = post(ruta).with(csrf());
        for (int i = 0; i + 1 < parametros.length; i += 2) {
            peticion.param(parametros[i], parametros[i + 1]);
        }
        return peticion;
    }

    MockHttpServletRequestBuilder crearFormulario(Sillon sillon, Paciente paciente, String inicio,
            boolean sobreturno) {
        return postConCsrf("/turnos/nuevo",
                "pacienteId", paciente.getId().toString(),
                "profesionalId", ana.getId().toString(),
                "sillonId", sillon.getId().toString(),
                "prestacionId", control.getId().toString(),
                "inicio", inicio,
                "sobreturno", Boolean.toString(sobreturno));
    }

    String htmlDe(String ruta) throws Exception {
        return mvc.perform(get(ruta)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    }

    String agendaDeAna() throws Exception {
        return htmlDe("/agenda?fecha=" + FECHA + "&profesionalId=" + ana.getId());
    }

    java.util.List<Turno> activosDeAna() {
        LocalDate dia = LocalDate.parse(FECHA);
        return turnos.buscarActivosDeProfesionalEntre(ana.getId(), dia.atStartOfDay(), dia.plusDays(1).atStartOfDay());
    }

    // Requisito: formulario de creación con errores de regla visibles.

    @Test
    void crearTurnoDesdeLaVistaYVerloEnLaAgenda() throws Exception {
        mvc.perform(crearFormulario(sillon1, paciente2, "2026-11-23T10:00", false))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/agenda?fecha=2026-11-23&profesionalId=" + ana.getId()));

        assertThat(turnos.count()).isEqualTo(1);
        Turno creado = activosDeAna().get(0);
        assertThat(creado.getEstado()).isEqualTo(TurnoEstado.RESERVADO);
        assertThat(creado.getFin()).isEqualTo(LocalDateTime.parse("2026-11-23T10:30"));

        assertThat(agendaDeAna())
                .contains("Paciente Numero 2")
                .contains("10:00 – 10:30")
                .doesNotContain("00000002");
    }

    @Test
    void choqueDeSillonSeMuestraEnElFormulario() throws Exception {
        guardarTurno(ana, sillon1, paciente1, "2026-11-23T09:30", "2026-11-23T10:00", false);

        mvc.perform(crearFormulario(sillon1, paciente2, "2026-11-23T09:45", false))
                .andExpect(status().isOk())
                .andExpect(view().name("turnos/nuevo"))
                .andExpect(content().string(containsString("CHOQUE_SILLON")))
                .andExpect(content().string(containsString("El sillón ya está ocupado")));

        assertThat(turnos.count()).isEqualTo(1);
    }

    @Test
    void sobreturnoEnOtroSillonSeCreaConSuMarca() throws Exception {
        guardarTurno(ana, sillon1, paciente1, "2026-11-23T09:30", "2026-11-23T10:00", false);

        mvc.perform(crearFormulario(sillon2, paciente2, "2026-11-23T09:45", true))
                .andExpect(status().is3xxRedirection());

        assertThat(turnos.count()).isEqualTo(2);
        assertThat(activosDeAna()).anySatisfy(t -> {
            assertThat(t.isSobreturno()).isTrue();
            assertThat(t.getSillon().getId()).isEqualTo(sillon2.getId());
        });
        assertThat(agendaDeAna()).contains("SOBRETURNO");
    }

    // Requisito: detalle y cancelación desde la vista.

    @Test
    void cancelarConAnticipacionSuficiente() throws Exception {
        fijar(LocalDateTime.parse("2026-11-22T20:00"));
        Turno turno = guardarTurno(ana, sillon1, paciente1, "2026-11-23T09:30", "2026-11-23T10:00", false);

        mvc.perform(postConCsrf("/turnos/" + turno.getId() + "/cancelar"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/turnos/" + turno.getId()));

        Turno cancelado = turnos.findById(turno.getId()).orElseThrow();
        assertThat(cancelado.getEstado()).isEqualTo(TurnoEstado.CANCELADO);
        assertThat(cancelado.getCanceladoEn()).isEqualTo(LocalDateTime.parse("2026-11-22T20:00"));
        assertThat(agendaDeAna()).doesNotContain("Paciente Numero 1");
        assertThat(htmlDe("/turnos/" + turno.getId())).contains("CANCELADO");
    }

    @Test
    void cancelacionTardiaMuestraElCodigoYNoModificaElTurno() throws Exception {
        fijar(LocalDateTime.parse("2026-11-23T03:31"));
        Turno turno = guardarTurno(ana, sillon1, paciente1, "2026-11-23T09:30", "2026-11-23T10:00", false);

        mvc.perform(postConCsrf("/turnos/" + turno.getId() + "/cancelar"))
                .andExpect(status().isOk())
                .andExpect(view().name("turnos/detalle"))
                .andExpect(content().string(containsString("CANCELACION_TARDIA")));

        assertThat(turnos.findById(turno.getId()).orElseThrow().getEstado()).isEqualTo(TurnoEstado.RESERVADO);
    }

    @Test
    void segundaCancelacionMuestraElCodigoYConservaLaFechaOriginal() throws Exception {
        fijar(LocalDateTime.parse("2026-11-22T20:00"));
        Turno turno = guardarTurno(ana, sillon1, paciente1, "2026-11-23T09:30", "2026-11-23T10:00", false);
        mvc.perform(postConCsrf("/turnos/" + turno.getId() + "/cancelar"))
                .andExpect(status().is3xxRedirection());
        LocalDateTime canceladoEn = turnos.findById(turno.getId()).orElseThrow().getCanceladoEn();

        mvc.perform(postConCsrf("/turnos/" + turno.getId() + "/cancelar"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("TURNO_YA_CANCELADO")));

        assertThat(turnos.findById(turno.getId()).orElseThrow().getCanceladoEn()).isEqualTo(canceladoEn);
    }

    // Requisito: acción de WhatsApp en el detalle.

    @Test
    void pacienteConTelefonoMuestraElLinkClickToChat() throws Exception {
        Turno turno = guardarTurno(ana, sillon1, paciente1, "2026-11-23T09:30", "2026-11-23T10:00", false);

        String html = htmlDe("/turnos/" + turno.getId());

        // El DNI no puede aparecer como dato de la vista; el teléfono sí va dentro de la URL wa.me (C-06).
        assertThat(html).contains("https://wa.me/").contains("Abrir WhatsApp")
                .doesNotContainPattern("(?<!\\d)00000001(?!\\d)");
    }

    @Test
    void pacienteSinTelefonoDeshabilitaLaAccionSinMostrarError() throws Exception {
        Turno turno = guardarTurno(ana, sillon1, sinTelefono, "2026-11-23T11:00", "2026-11-23T11:30", false);

        String html = htmlDe("/turnos/" + turno.getId());

        assertThat(html).contains("no tiene teléfono cargado")
                .contains("disabled")
                .doesNotContain("https://wa.me/")
                .doesNotContain("PACIENTE_SIN_TELEFONO");
    }

    // Requisito: agenda del día por rol.

    @Test
    void agendaMuestraSoloLosActivosOrdenadosSinDatosPersonales() throws Exception {
        guardarTurno(ana, sillon1, paciente1, "2026-11-23T09:00", "2026-11-23T09:30", false);
        Turno cancelado = guardarTurno(ana, sillon1, paciente1, "2026-11-23T10:00", "2026-11-23T10:30", false);
        cancelado.marcarCancelado(LocalDateTime.parse("2026-11-22T10:00"));
        turnos.save(cancelado);
        guardarTurno(ana, sillon2, paciente2, "2026-11-23T11:30", "2026-11-23T12:00", true);

        String html = agendaDeAna();

        assertThat(html).contains("Paciente Numero 1").contains("Paciente Numero 2")
                .contains("09:00 – 09:30").contains("11:30 – 12:00").contains("SOBRETURNO")
                .doesNotContain("10:00 – 10:30")
                .doesNotContain("00000001").doesNotContain("5491100000001");
        assertThat(html.indexOf("09:00 – 09:30")).isLessThan(html.indexOf("11:30 – 12:00"));
    }

    @Test
    @WithMockUser(username = "ana", roles = "ODONTOLOGO")
    void odontologoNoPuedeAbrirLaAgendaDeOtroProfesional() throws Exception {
        como(new Solicitante("ana", Rol.ODONTOLOGO, ana.getId()));
        guardarTurno(bruno, sillon1, paciente1, "2026-11-23T09:00", "2026-11-23T09:30", false);

        mvc.perform(get("/agenda")
                        .param("fecha", FECHA)
                        .param("profesionalId", bruno.getId().toString()))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("ACCESO_DENEGADO")))
                .andExpect(content().string(not(containsString("Paciente Numero 1"))));
    }

    @Test
    void diaSinTurnosMuestraUnEstadoVacio() throws Exception {
        String html = htmlDe("/agenda?fecha=2026-11-24&profesionalId=" + bruno.getId());

        assertThat(html).contains("No hay turnos RESERVADOS").doesNotContain("Paciente Numero 1");
    }

    @Test
    void agendaEstaDisponibleParaLosTresRoles() throws Exception {
        como(new Solicitante("admin", Rol.ADMIN, null));
        mvc.perform(get("/agenda").with(user("admin").roles("ADMIN"))).andExpect(status().isOk());

        como(new Solicitante("secretaria", Rol.SECRETARIA, null));
        mvc.perform(get("/agenda").with(user("secretaria").roles("SECRETARIA"))).andExpect(status().isOk());

        como(new Solicitante("ana", Rol.ODONTOLOGO, ana.getId()));
        mvc.perform(get("/agenda").with(user("ana").roles("ODONTOLOGO"))).andExpect(status().isOk());
    }

    // Requisito: seguridad de las vistas de turnos y agenda.

    @Test
    void formularioSinTokenCsrfEsRechazado() throws Exception {
        mvc.perform(post("/turnos/nuevo")
                        .param("pacienteId", paciente2.getId().toString())
                        .param("profesionalId", ana.getId().toString())
                        .param("sillonId", sillon1.getId().toString())
                        .param("prestacionId", control.getId().toString())
                        .param("inicio", "2026-11-23T10:00"))
                .andExpect(status().isForbidden());

        assertThat(turnos.count()).isZero();
    }

    @Test
    @WithMockUser(username = "ana", roles = "ODONTOLOGO")
    void odontologoNoAccedeACrearNiCancelarNiAlLinkDeWhatsapp() throws Exception {
        como(new Solicitante("ana", Rol.ODONTOLOGO, ana.getId()));
        Turno turno = guardarTurno(ana, sillon1, paciente1, "2026-11-23T09:30", "2026-11-23T10:00", false);

        mvc.perform(get("/turnos/nuevo")).andExpect(status().isForbidden());
        mvc.perform(postConCsrf("/turnos/" + turno.getId() + "/cancelar"))
                .andExpect(status().isForbidden());

        String detalle = htmlDe("/turnos/" + turno.getId());
        assertThat(detalle).doesNotContain("Cancelar turno")
                .doesNotContain("Abrir WhatsApp")
                .doesNotContain("https://wa.me/");
        assertThat(turnos.findById(turno.getId()).orElseThrow().getEstado()).isEqualTo(TurnoEstado.RESERVADO);
    }

    @Test
    void adminTambienPuedeCrearTurnos() throws Exception {
        como(new Solicitante("admin", Rol.ADMIN, null));

        mvc.perform(crearFormulario(sillon1, paciente2, "2026-11-23T12:00", false)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().is3xxRedirection());

        assertThat(turnos.count()).isEqualTo(1);
    }
}
