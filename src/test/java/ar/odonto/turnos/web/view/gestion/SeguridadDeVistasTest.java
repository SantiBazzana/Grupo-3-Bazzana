package ar.odonto.turnos.web.view.gestion;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * CSRF de los formularios de la interfaz (4.1) y autorización por ruta de las vistas según
 * la matriz RBAC (4.2). El POST de vista sin token debe rechazarse; con token debe procesarse.
 */
@org.springframework.boot.test.context.SpringBootTest
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
@org.springframework.transaction.annotation.Transactional
class SeguridadDeVistasTest {

    @Autowired MockMvc mvc;

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void postDeVistaSinTokenCsrfEsRechazado() throws Exception {
        mvc.perform(post("/gestion/pacientes")
                        .param("apellido", "Uno")
                        .param("nombre", "Paciente")
                        .param("dni", "00000033"))
                .andExpect(status().isForbidden());
    }

    @Test
    void postDeLoginSinTokenCsrfEsRechazado() throws Exception {
        mvc.perform(post("/login")
                        .param("username", "cualquiera")
                        .param("password", "cualquiera"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void postDeVistaConTokenCsrfSeProcesa() throws Exception {
        mvc.perform(post("/gestion/sillones").with(
                        org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .param("nombre", "Sillón CSRF"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/gestion/sillones"));
    }

    @Test
    @WithMockUser(username = "ana", roles = "ODONTOLOGO")
    void odontologoRecibeAccesoDenegadoEnPacientes() throws Exception {
        mvc.perform(get("/gestion/pacientes")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "secretaria", roles = "SECRETARIA")
    void secretariaRecibeAccesoDenegadoAlEditarHorario() throws Exception {
        mvc.perform(post("/gestion/horario").with(
                        org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .param("diaSemana", "MONDAY")
                        .param("apertura", "08:00")
                        .param("cierre", "12:00"))
                .andExpect(status().isForbidden());
    }

    @Test
    void vistaDeGestionSinSesionRedirigeAlLogin() throws Exception {
        mvc.perform(get("/gestion/profesionales"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void apiSinSesionDevuelveProblemDetailNoAutenticado() throws Exception {
        mvc.perform(get("/api/pacientes"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.codigo").value("NO_AUTENTICADO"));
    }
}
