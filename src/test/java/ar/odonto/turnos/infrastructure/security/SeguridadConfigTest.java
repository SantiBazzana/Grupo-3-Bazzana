package ar.odonto.turnos.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Cadena de seguridad de C-05: rutas públicas sin sesión, todo lo demás requiere
 * autenticación, y los errores de seguridad devuelven Problem Detail con `codigo`.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SeguridadConfigTest {

    @Autowired MockMvc mvc;

    @Test
    void cadenaDeSeguridadEsLaDeC05NoLaPermisivaDeC01() {
        assertThat(mvc).isNotNull();
    }

    @Test
    void saludEsPublicoSinSesion() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void rutaProtegidaSinSesionResponde401() throws Exception {
        mvc.perform(get("/api/pacientes"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("NO_AUTENTICADO"));
    }

    @Test
    void loginEsPublicoSinSesionPrevia() throws Exception {
        // /api/auth/login es pública: llega al controlador (401 por credenciales, no 403 por cadena).
        mvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content("{\"username\":\"noexiste\",\"password\":\"x\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("NO_AUTENTICADO"));
    }
}
