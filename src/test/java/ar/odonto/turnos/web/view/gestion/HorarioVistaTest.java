package ar.odonto.turnos.web.view.gestion;

import ar.odonto.turnos.domain.model.Consultorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import java.time.DayOfWeek;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/** Pantalla de horario: feliz (franja), error de negocio (franja invertida) y borde (cierre de día). */
class HorarioVistaTest extends BaseDeVistasDeGestion {

    private Consultorio consultorio;

    @BeforeEach
    void consultorioDePrueba() {
        consultorio = consultorios.save(new Consultorio("Consultorio Demo", "America/Argentina/Buenos_Aires"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminReemplazaLaFranjaDeUnDia() throws Exception {
        mvc.perform(postConCsrf("/gestion/horario",
                        "diaSemana", "MONDAY", "apertura", "08:00", "cierre", "12:00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/gestion/horario"));

        String html = htmlDe("/gestion/horario");
        assertThat(html).contains("08:00").contains("12:00");

        assertThat(horarios.findByConsultorioIdAndDiaSemana(consultorio.getId(), DayOfWeek.MONDAY))
                .isPresent();
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void horarioInvalidoMuestraElErrorDeLaRegla() throws Exception {
        MvcResult resultado = mvc.perform(postConCsrf("/gestion/horario",
                        "diaSemana", "MONDAY", "apertura", "18:00", "cierre", "09:00"))
                .andExpect(status().isOk())
                .andExpect(view().name("gestion/horario"))
                .andReturn();

        assertThat(resultado.getModelAndView().getModel()).containsEntry("errorCodigo", "HORARIO_INVALIDO");
        assertThat(resultado.getResponse().getContentAsString()).contains("HORARIO_INVALIDO");
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminCierraUnDiaDejandoLaFranjaVacia() throws Exception {
        horarios.save(new ar.odonto.turnos.domain.model.HorarioAtencion(consultorio, DayOfWeek.SATURDAY,
                java.time.LocalTime.of(9, 0), java.time.LocalTime.of(13, 0)));

        mvc.perform(postConCsrf("/gestion/horario",
                        "diaSemana", "SATURDAY", "apertura", "", "cierre", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/gestion/horario"));

        assertThat(horarios.findByConsultorioIdAndDiaSemana(consultorio.getId(), DayOfWeek.SATURDAY))
                .isEmpty();
    }

    @Test
    @WithMockUser(username = "odontologo", roles = "ODONTOLOGO")
    void odontologoNoPuedeEditarElHorario() throws Exception {
        mvc.perform(postConCsrf("/gestion/horario",
                        "diaSemana", "MONDAY", "apertura", "08:00", "cierre", "12:00"))
                .andExpect(status().isForbidden());
    }
}
