package ar.odonto.turnos.web.view.gestion;

import ar.odonto.turnos.domain.model.Profesional;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/** Pantalla de profesionales: feliz (lectura y alta), error de negocio (matrícula duplicada) y borde (RBAC). */
class ProfesionalesVistaTest extends BaseDeVistasDeGestion {

    @Test
    @WithMockUser(username = "secretaria", roles = "SECRETARIA")
    void secretariaVeElListadoEnModoLecrua() throws Exception {
        profesionales.save(new Profesional("Ana", "Ejemplo", "MP-0001", "5491100000101"));

        String html = htmlDe("/gestion/profesionales");

        assertThat(html)
                .contains("MP-0001")
                .contains("Ana")
                .contains("class=\"tabla-scroll\"")
                .doesNotContain("id=\"form-alta-profesional\"");
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminDaDeAltaUnProfesional() throws Exception {
        mvc.perform(postConCsrf("/gestion/profesionales",
                        "nombre", "Ana", "apellido", "Ejemplo", "matricula", "MP-0100", "telefono", "5491100000100"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/gestion/profesionales"));

        assertThat(profesionales.existsByMatricula("MP-0100")).isTrue();
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void matriculaDuplicadaMuestraElErrorDeLaRegla() throws Exception {
        profesionales.save(new Profesional("Ana", "Ejemplo", "MP-0002", "5491100000102"));

        MvcResult resultado = mvc.perform(postConCsrf("/gestion/profesionales",
                        "nombre", "Bruno", "apellido", "Prueba", "matricula", "MP-0002"))
                .andExpect(status().isOk())
                .andExpect(view().name("gestion/profesionales"))
                .andReturn();

        assertThat(resultado.getModelAndView().getModel()).containsEntry("errorCodigo", "DATO_DUPLICADO");
        assertThat(resultado.getResponse().getContentAsString()).contains("DATO_DUPLICADO");
    }

    @Test
    @WithMockUser(username = "secretaria", roles = "SECRETARIA")
    void secretariaNoPuedeDarDeAltaUnProfesional() throws Exception {
        mvc.perform(postConCsrf("/gestion/profesionales",
                        "nombre", "Ana", "apellido", "Ejemplo", "matricula", "MP-9999"))
                .andExpect(status().isForbidden());

        assertThat(profesionales.existsByMatricula("MP-9999")).isFalse();
    }
}
