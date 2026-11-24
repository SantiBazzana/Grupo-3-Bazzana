package ar.odonto.turnos.web.view.gestion;

import ar.odonto.turnos.domain.model.Paciente;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Pantalla de pacientes: feliz (la secretaría ve la sección y da de alta), error de negocio
 * (DNI duplicado muestra el código de la regla) y borde (el odontólogo no accede).
 */
class PacientesVistaTest extends BaseDeVistasDeGestion {

    @Test
    @WithMockUser(username = "secretaria", roles = "SECRETARIA")
    void secretariaVeLaSeccionDePacientes() throws Exception {
        pacientes.save(new Paciente("Paciente", "Uno", "00000001", "5491100000001", null));

        String html = htmlDe("/gestion/pacientes");

        assertThat(html)
                .contains("00000001")
                .contains("Paciente")
                .contains("class=\"buscador\"")
                .contains("class=\"tabla-scroll\"")
                .contains("id=\"form-alta-paciente\"");
    }

    @Test
    @WithMockUser(username = "secretaria", roles = "SECRETARIA")
    void secretariaDaDeAltaUnPaciente() throws Exception {
        mvc.perform(postConCsrf("/gestion/pacientes",
                        "apellido", "Uno", "nombre", "Paciente", "dni", "00000009",
                        "telefono", "5491100000009", "email", "paciente.uno@example.test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/gestion/pacientes"));

        assertThat(pacientes.existsByDni("00000009")).isTrue();
    }

    @Test
    @WithMockUser(username = "secretaria", roles = "SECRETARIA")
    void dniDuplicadoMuestraElErrorDeLaRegla() throws Exception {
        pacientes.save(new Paciente("Paciente", "Uno", "00000002", "5491100000002", null));

        MvcResult resultado = mvc.perform(postConCsrf("/gestion/pacientes",
                        "apellido", "Dos", "nombre", "Paciente", "dni", "00000002"))
                .andExpect(status().isOk())
                .andExpect(view().name("gestion/pacientes"))
                .andReturn();

        assertThat(resultado.getModelAndView().getModel()).containsEntry("errorCodigo", "DATO_DUPLICADO");
        assertThat(resultado.getResponse().getContentAsString()).contains("DATO_DUPLICADO");
    }

    @Test
    @WithMockUser(username = "secretaria", roles = "SECRETARIA")
    void laBusquedaFiltraPorDni() throws Exception {
        pacientes.save(new Paciente("Paciente", "Uno", "00000001", "5491100000001", null));
        pacientes.save(new Paciente("Paciente", "Dos", "00000002", "5491100000002", null));

        String html = mvc.perform(get("/gestion/pacientes").param("q", "00000002"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(html).contains("00000002").doesNotContain("00000001");
    }

    @Test
    @WithMockUser(username = "ana", roles = "ODONTOLOGO")
    void odontologoNoAccedeALaPantallaDePacientes() throws Exception {
        mvc.perform(get("/gestion/pacientes")).andExpect(status().isForbidden());
        mvc.perform(postConCsrf("/gestion/pacientes", "apellido", "Uno", "nombre", "Paciente", "dni", "00000077"))
                .andExpect(status().isForbidden());

        assertThat(pacientes.existsByDni("00000077")).isFalse();
    }
}
