package ar.odonto.turnos.web.view.gestion;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Escenarios del spec (uno por test): feliz (el panel renderiza el layout base) y borde
 * (sin sesión no se accede al panel). Triangula además la visibilidad por rol del panel.
 */
@SpringBootTest
@AutoConfigureMockMvc
class LayoutYPanelTest {

    @Autowired MockMvc mvc;

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void panelRenderizaElLayoutConNavegacionYEstilos() throws Exception {
        MvcResult resultado = mvc.perform(get("/panel"))
                .andExpect(status().isOk())
                .andExpect(view().name("gestion/panel"))
                .andReturn();

        String html = resultado.getResponse().getContentAsString();
        assertThat(html)
                .contains("Turnos Odontología")
                .contains("class=\"contenedor\"")
                .contains("/css/estilos.css")
                .contains("href=\"/gestion/pacientes\"")
                .contains("href=\"/gestion/profesionales\"")
                .contains("href=\"/gestion/sillones\"")
                .contains("href=\"/gestion/prestaciones\"")
                .contains("href=\"/gestion/horario\"");
    }

    @Test
    @WithMockUser(username = "secretaria", roles = "SECRETARIA")
    void secretariaVePacientesEnLaNavegacion() throws Exception {
        String html = mvc.perform(get("/panel"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(html).contains("href=\"/gestion/pacientes\"");
    }

    @Test
    @WithMockUser(username = "ana", roles = "ODONTOLOGO")
    void odontologoNoVeLaSeccionDePacientes() throws Exception {
        String html = mvc.perform(get("/panel"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(html)
                .doesNotContain("href=\"/gestion/pacientes\"")
                .contains("href=\"/gestion/profesionales\"");
    }

    @Test
    void navegacionDeTurnosRespetaElRolAutenticado() throws Exception {
        String admin = htmlDePanel("admin", "ADMIN");
        String secretaria = htmlDePanel("secretaria", "SECRETARIA");
        String odontologo = htmlDePanel("ana", "ODONTOLOGO");

        assertThat(admin)
                .contains("href=\"/agenda\"").contains(">Agenda</a>")
                .contains("href=\"/turnos/nuevo\"").contains(">Nuevo turno</a>");
        assertThat(secretaria)
                .contains("href=\"/agenda\"").contains(">Agenda</a>")
                .contains("href=\"/turnos/nuevo\"").contains(">Nuevo turno</a>");
        assertThat(odontologo)
                .contains("href=\"/agenda\"").contains(">Agenda</a>")
                .doesNotContain("href=\"/turnos/nuevo\"")
                .doesNotContain(">Nuevo turno</a>");
    }

    private String htmlDePanel(String usuario, String rol) throws Exception {
        return mvc.perform(get("/panel").with(user(usuario).roles(rol)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    void panelSinSesionRedirigeAlLogin() throws Exception {
        mvc.perform(get("/panel"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void raizConSesionRedirigeAlPanel() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/panel"));
    }

    @Test
    void raizSinSesionRedirigeAlLogin() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}
