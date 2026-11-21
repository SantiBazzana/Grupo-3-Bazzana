package ar.odonto.turnos.web.api.configuracion;

import ar.odonto.turnos.domain.model.Sillon;
import ar.odonto.turnos.support.DatosDePrueba;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** No se exponen operaciones DELETE: la baja es lógica (spec: configuracion-consultorio). */
class SillonesApiTest extends ConfiguracionApiTestBase {

    @Test
    void deleteNoDisponible() throws Exception {
        Sillon sillon = sillones.save(DatosDePrueba.sillon("Sillon 1"));

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/sillones/" + sillon.getId()))
                .andExpect(status().is4xxClientError());

        getJson("/api/sillones")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(sillon.getId()));
        assertThat(sillones.count()).isEqualTo(1);
    }

    @Test
    void deleteDeProfesionalTampocoDisponible() throws Exception {
        Long id = profesionales.save(DatosDePrueba.profesional("M-0011")).getId();

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/profesionales/" + id))
                .andExpect(status().is4xxClientError());

        getJson("/api/profesionales")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id));
        assertThat(profesionales.count()).isEqualTo(1);
    }

    @Test
    void deleteSinIdTampocoDisponible() throws Exception {
        sillones.save(DatosDePrueba.sillon("Sillon 2"));

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/sillones"))
                .andExpect(status().is4xxClientError());

        assertThat(sillones.count()).isEqualTo(1);
    }
}
