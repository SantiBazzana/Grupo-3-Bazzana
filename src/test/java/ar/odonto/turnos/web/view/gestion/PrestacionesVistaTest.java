package ar.odonto.turnos.web.view.gestion;

import ar.odonto.turnos.domain.model.Prestacion;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/** Pantalla de prestaciones: feliz (alta), error de negocio (duración inválida) y borde (RBAC). */
class PrestacionesVistaTest extends BaseDeVistasDeGestion {

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminDaDeAltaUnaPrestacion() throws Exception {
        prestaciones.save(new Prestacion("Control", 30));

        assertThat(htmlDe("/gestion/prestaciones")).contains("Control").contains("30");

        mvc.perform(postConCsrf("/gestion/prestaciones", "nombre", "Limpieza", "duracionMinutos", "45"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/gestion/prestaciones"));

        assertThat(prestaciones.existsByNombre("Limpieza")).isTrue();
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void duracionInvalidaMuestraElErrorDeLaRegla() throws Exception {
        MvcResult resultado = mvc.perform(postConCsrf("/gestion/prestaciones",
                        "nombre", "Práctica", "duracionMinutos", "7"))
                .andExpect(status().isOk())
                .andExpect(view().name("gestion/prestaciones"))
                .andReturn();

        assertThat(resultado.getModelAndView().getModel()).containsEntry("errorCodigo", "DURACION_INVALIDA");
        assertThat(resultado.getResponse().getContentAsString()).contains("DURACION_INVALIDA");
        assertThat(prestaciones.existsByNombre("Práctica")).isFalse();
    }

    @Test
    @WithMockUser(username = "secretaria", roles = "SECRETARIA")
    void secretariaNoPuedeDarDeAltaUnaPrestacion() throws Exception {
        mvc.perform(postConCsrf("/gestion/prestaciones", "nombre", "Limpieza", "duracionMinutos", "45"))
                .andExpect(status().isForbidden());

        assertThat(prestaciones.existsByNombre("Limpieza")).isFalse();
    }
}
