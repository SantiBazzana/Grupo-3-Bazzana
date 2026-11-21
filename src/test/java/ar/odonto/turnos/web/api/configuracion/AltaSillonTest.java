package ar.odonto.turnos.web.api.configuracion;

import ar.odonto.turnos.support.DatosDePrueba;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Escenarios de alta de sillones (spec: configuracion-consultorio). */
class AltaSillonTest extends ConfiguracionApiTestBase {

    @Test
    void altaDeSillonUno() throws Exception {
        postJson("/api/sillones", """
                {"nombre":"Sillon 1"}
                """)
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern(".*/api/sillones/\\d+")))
                .andExpect(jsonPath("$.nombre").value("Sillon 1"))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    void altaDeSillonDos() throws Exception {
        postJson("/api/sillones", """
                {"nombre":"Sillon 2"}
                """)
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern(".*/api/sillones/\\d+")))
                .andExpect(jsonPath("$.nombre").value("Sillon 2"))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    void nombreDuplicado() throws Exception {
        sillones.save(DatosDePrueba.sillon("Sillon 1"));

        codigo(postJson("/api/sillones", """
                {"nombre":"Sillon 1"}
                """), 409, "DATO_DUPLICADO");

        assertThat(sillones.count()).isEqualTo(1);
    }

    @Test
    void capitalizacionDistintaEsOtroNombre() throws Exception {
        postJson("/api/sillones", """
                {"nombre":"sillon 3"}
                """).andExpect(status().isCreated());
        postJson("/api/sillones", """
                {"nombre":"Sillon 3"}
                """).andExpect(status().isCreated());

        assertThat(sillones.count()).isEqualTo(2);
    }

    @Test
    void nombreConEspacioSeNormaliza() throws Exception {
        sillones.save(DatosDePrueba.sillon("Sillon 4"));

        codigo(postJson("/api/sillones", """
                {"nombre":"Sillon 4 "}
                """), 409, "DATO_DUPLICADO");

        assertThat(sillones.count()).isEqualTo(1);
    }
}
