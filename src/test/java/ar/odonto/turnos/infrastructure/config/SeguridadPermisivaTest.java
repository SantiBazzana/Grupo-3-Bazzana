package ar.odonto.turnos.infrastructure.config;

import ar.odonto.turnos.domain.error.CodigoError;
import ar.odonto.turnos.domain.error.ReglaDeNegocioException;
import ar.odonto.turnos.domain.port.UsuarioActual;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Seguridad de C-05 reemplaza a la permisiva de C-01 mediante {@code @ConditionalOnMissingBean}:
 * cuando la aplicación declara su propia cadena (lo que hace C-05), la de C-01 se retira sin
 * editar archivos compartidos.
 */
class SeguridadPermisivaTest {

    @Nested
    @SpringBootTest
    @AutoConfigureMockMvc
    class SinConfiguracionPropia {

        @Autowired MockMvc mvc;
        @Autowired UsuarioActual usuarioActual;
        @Autowired ApplicationContext contexto;

        @Test
        void laCadenaDeSeguridadEsLaDeC05YNoLaPermisivaDeC01() {
            assertThat(contexto.getBeansOfType(SecurityFilterChain.class))
                    .containsOnlyKeys("cadenaDeSeguridad");
        }

        @Test
        void unaRutaProtegidaSinCredencialesResponde401() throws Exception {
            mvc.perform(get("/api/pacientes"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.codigo").value("NO_AUTENTICADO"));
        }

        @Test
        void elSolicitanteRequiereAutenticacion() {
            try {
                usuarioActual.solicitante();
                throw new AssertionError("debería haber lanzado NO_AUTENTICADO");
            } catch (ReglaDeNegocioException ex) {
                assertThat(ex.codigo()).isEqualTo(CodigoError.NO_AUTENTICADO);
            }
        }
    }
}
