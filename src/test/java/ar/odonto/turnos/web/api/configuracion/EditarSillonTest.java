package ar.odonto.turnos.web.api.configuracion;

import ar.odonto.turnos.domain.model.Sillon;
import ar.odonto.turnos.support.DatosDePrueba;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Escenarios de edición de sillones (spec: configuracion-consultorio). */
class EditarSillonTest extends ConfiguracionApiTestBase {

    @Test
    void editarMismoNombreNoDuplicado() throws Exception {
        Sillon sillon = sillones.save(DatosDePrueba.sillon("Sillon 1"));

        putJson("/api/sillones/" + sillon.getId(), """
                {"nombre":"Sillon 1","activo":false}
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sillon.getId()))
                .andExpect(jsonPath("$.nombre").value("Sillon 1"))
                .andExpect(jsonPath("$.activo").value(false));

        assertThat(sillones.findById(sillon.getId()).orElseThrow().isActivo()).isFalse();
    }

    @Test
    void editarSoloActivo() throws Exception {
        Sillon sillon = sillones.save(DatosDePrueba.sillon("Sillon 5"));
        sillon.desactivar();

        putJson("/api/sillones/" + sillon.getId(), """
                {"activo":true}
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Sillon 5"))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    void editarInexistente() throws Exception {
        codigo(putJson("/api/sillones/999999", """
                {"activo":false}
                """), 404, "RECURSO_INEXISTENTE");
    }

    @Test
    void editarOtroIdInexistente() throws Exception {
        codigo(putJson("/api/sillones/999998", """
                {"nombre":"Sillon 9"}
                """), 404, "RECURSO_INEXISTENTE");
    }
}
