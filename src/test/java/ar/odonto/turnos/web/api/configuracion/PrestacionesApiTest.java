package ar.odonto.turnos.web.api.configuracion;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/** Validación de entrada de prestaciones (spec: configuracion-consultorio). */
class PrestacionesApiTest extends ConfiguracionApiTestBase {

    @Test
    void crearSinNombre() throws Exception {
        codigo(postJson("/api/prestaciones", """
                {"duracionMinutos":30}
                """), 400, "ENTRADA_INVALIDA");

        postJson("/api/prestaciones", """
                {"duracionMinutos":30}
                """)
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("nombre")));
        assertThat(prestaciones.count()).isZero();
    }

    @Test
    void crearSinDuracion() throws Exception {
        codigo(postJson("/api/prestaciones", """
                {"nombre":"Control"}
                """), 400, "ENTRADA_INVALIDA");

        postJson("/api/prestaciones", """
                {"nombre":"Control"}
                """)
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("duracionMinutos")));
        assertThat(prestaciones.count()).isZero();
    }
}
