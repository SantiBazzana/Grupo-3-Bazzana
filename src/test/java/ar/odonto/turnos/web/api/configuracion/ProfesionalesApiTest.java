package ar.odonto.turnos.web.api.configuracion;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Alta de profesional sin vínculo de usuario (decisión c, spec: configuracion-consultorio). */
class ProfesionalesApiTest extends ConfiguracionApiTestBase {

    @Test
    void altaNoCreaUsuario() throws Exception {
        long usuariosAntes = usuarios.count();

        postJson("/api/profesionales", """
                {"nombre":"Carla","apellido":"Ejemplo","matricula":"M-0006","telefono":"5491100000006"}
                """)
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern(".*/api/profesionales/\\d+")))
                .andExpect(jsonPath("$.matricula").value("M-0006"))
                .andExpect(jsonPath("$.usuario").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.usuarioId").doesNotExist());

        assertThat(usuarios.count()).isEqualTo(usuariosAntes);
    }

    @Test
    void altaSinTelefonoNoCreaUsuario() throws Exception {
        long usuariosAntes = usuarios.count();

        postJson("/api/profesionales", """
                {"nombre":"Diego","apellido":"Ejemplo","matricula":"M-0007"}
                """)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuarioId").doesNotExist());

        assertThat(usuarios.count()).isEqualTo(usuariosAntes);
    }
}
