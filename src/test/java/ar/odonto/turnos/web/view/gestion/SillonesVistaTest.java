package ar.odonto.turnos.web.view.gestion;

import ar.odonto.turnos.domain.model.Sillon;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/** Pantalla de sillones: feliz (lista + alta), error de negocio (nombre duplicado) y borde (RBAC). */
class SillonesVistaTest extends BaseDeVistasDeGestion {

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminVeElListadoYDaDeAltaUnSillon() throws Exception {
        sillones.save(new Sillon("Sillón 1"));

        assertThat(htmlDe("/gestion/sillones")).contains("Sillón 1");

        mvc.perform(postConCsrf("/gestion/sillones", "nombre", "Sillón 9"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/gestion/sillones"));

        assertThat(sillones.existsByNombre("Sillón 9")).isTrue();
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void nombreDuplicadoMuestraElErrorDeLaRegla() throws Exception {
        sillones.save(new Sillon("Sillón 1"));

        MvcResult resultado = mvc.perform(postConCsrf("/gestion/sillones", "nombre", "Sillón 1"))
                .andExpect(status().isOk())
                .andExpect(view().name("gestion/sillones"))
                .andReturn();

        assertThat(resultado.getModelAndView().getModel()).containsEntry("errorCodigo", "DATO_DUPLICADO");
        assertThat(resultado.getResponse().getContentAsString()).contains("DATO_DUPLICADO");
    }

    @Test
    @WithMockUser(username = "secretaria", roles = "SECRETARIA")
    void secretariaNoPuedeDarDeAltaUnSillon() throws Exception {
        mvc.perform(postConCsrf("/gestion/sillones", "nombre", "Sillón 9"))
                .andExpect(status().isForbidden());

        assertThat(sillones.existsByNombre("Sillón 9")).isFalse();
    }
}
