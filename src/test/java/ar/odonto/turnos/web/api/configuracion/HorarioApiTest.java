package ar.odonto.turnos.web.api.configuracion;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.containsString;

/** Validación de entrada del horario (spec: configuracion-consultorio). */
class HorarioApiTest extends ConfiguracionApiTestBase {

    @Test
    void diaInvalido() throws Exception {
        putJson("/api/consultorio/horario", """
                {"diaSemana":"FUNDAY","apertura":"08:00","cierre":"20:00"}
                """)
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isBadRequest())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .jsonPath("$.codigo").value("ENTRADA_INVALIDA"));
    }

    @Test
    void diaVacio() throws Exception {
        putJson("/api/consultorio/horario", """
                {"diaSemana":"","apertura":"08:00","cierre":"20:00"}
                """)
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isBadRequest())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .jsonPath("$.detail").value(containsString("diaSemana")));
    }
}
