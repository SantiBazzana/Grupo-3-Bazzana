package ar.odonto.turnos.web.api.error;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Spec base-del-sistema: "Formato uniforme de errores" y "Entrada mal formada".
 * Las solicitudes llevan usuario y CSRF para que el test no dependa del modo de seguridad vigente.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ManejadorGlobalDeErroresTest {

    @Autowired
    MockMvc mvc;

    @Test
    void violacionDeRegla_respondeProblemDetailConSuCodigoYEstado() throws Exception {
        mvc.perform(get("/test/errores/regla/CHOQUE_SILLON").with(user("prueba")))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.codigo").value("CHOQUE_SILLON"))
                .andExpect(jsonPath("$.title").isNotEmpty())
                .andExpect(jsonPath("$.detail").isNotEmpty());
    }

    @Test
    void reglaDeNegocio422_respondeConSuCodigo() throws Exception {
        mvc.perform(get("/test/errores/regla/TURNO_EN_PASADO").with(user("prueba")))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.codigo").value("TURNO_EN_PASADO"));
    }

    @Test
    void campoObligatorioAusente_responde400ConElNombreDelCampo() throws Exception {
        mvc.perform(post("/test/errores/validacion").with(user("prueba")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.codigo").value("ENTRADA_INVALIDA"))
                .andExpect(jsonPath("$.detail").value(containsString("nombre")));
    }

    @Test
    void jsonIlegible_responde400EntradaInvalida() throws Exception {
        mvc.perform(post("/test/errores/validacion").with(user("prueba")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{esto no es json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("ENTRADA_INVALIDA"));
    }

    @Test
    void tipoDeDatoIncorrecto_responde400EntradaInvalida() throws Exception {
        mvc.perform(get("/test/errores/regla/NO_EXISTE_ESTE_CODIGO").with(user("prueba")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("ENTRADA_INVALIDA"));
    }

    @Test
    void errorInesperado_responde500SinTrazaNiDatosSensibles() throws Exception {
        mvc.perform(get("/test/errores/inesperado").with(user("prueba")))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.codigo").value("ERROR_INTERNO"))
                .andExpect(content().string(not(containsString("IllegalStateException"))))
                .andExpect(content().string(not(containsString("00000001"))))
                .andExpect(content().string(not(containsString("at ar.odonto"))));
    }
}
