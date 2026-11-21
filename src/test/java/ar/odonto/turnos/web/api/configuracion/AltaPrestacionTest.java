package ar.odonto.turnos.web.api.configuracion;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Escenarios de alta de prestación (spec: configuracion-consultorio). */
class AltaPrestacionTest extends ConfiguracionApiTestBase {

    @Test
    void altaDeControl30Minutos() throws Exception {
        postJson("/api/prestaciones", """
                {"nombre":"Control","duracionMinutos":30}
                """)
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern(".*/api/prestaciones/\\d+")))
                .andExpect(jsonPath("$.nombre").value("Control"))
                .andExpect(jsonPath("$.duracionMinutos").value(30))
                .andExpect(jsonPath("$.activa").value(true))
                .andExpect(jsonPath("$.dni").doesNotExist());
    }

    @Test
    void duracion7Rechazada() throws Exception {
        codigo(postJson("/api/prestaciones", """
                {"nombre":"Control","duracionMinutos":7}
                """), 422, "DURACION_INVALIDA");
        assertThat(prestaciones.count()).isZero();
    }

    @Test
    void duracionCeroRechazada() throws Exception {
        codigo(postJson("/api/prestaciones", """
                {"nombre":"Control","duracionMinutos":0}
                """), 422, "DURACION_INVALIDA");
        assertThat(prestaciones.count()).isZero();
    }

    @Test
    void duracion11Rechazada() throws Exception {
        codigo(postJson("/api/prestaciones", """
                {"nombre":"Radiografia","duracionMinutos":11}
                """), 422, "DURACION_INVALIDA");
        assertThat(prestaciones.count()).isZero();
    }

    @Test
    void duracionNegativaRechazada() throws Exception {
        codigo(postJson("/api/prestaciones", """
                {"nombre":"Control","duracionMinutos":-5}
                """), 422, "DURACION_INVALIDA");
        assertThat(prestaciones.count()).isZero();
    }

    @Test
    void altaDeLimpieza45Minutos() throws Exception {
        postJson("/api/prestaciones", """
                {"nombre":"Limpieza","duracionMinutos":45}
                """)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Limpieza"))
                .andExpect(jsonPath("$.duracionMinutos").value(45))
                .andExpect(jsonPath("$.activa").value(true));
    }
}
