package ar.odonto.turnos.infrastructure.config;

import ar.odonto.turnos.domain.model.Solicitante;
import ar.odonto.turnos.domain.port.UsuarioActual;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tarea 5.2 de C-01 (design D8): seguridad permisiva mientras no exista C-05, y reemplazo automático
 * cuando la aplicación declara su propia cadena (lo que hará C-05).
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
        void unEndpointRespondeSinCredenciales() throws Exception {
            // Llega al controlador (422 de negocio), no lo frena la seguridad (401/403).
            mvc.perform(get("/test/errores/regla/TURNO_EN_PASADO"))
                    .andExpect(status().isUnprocessableContent())
                    .andExpect(jsonPath("$.codigo").value("TURNO_EN_PASADO"));
        }

        @Test
        void elSolicitantePorDefectoEsSistemaConRolAdmin() {
            assertThat(usuarioActual.solicitante()).isEqualTo(Solicitante.SISTEMA);
        }

        @Test
        void hayUnaSolaCadenaDeSeguridad_laPermisiva() {
            assertThat(contexto.getBeansOfType(SecurityFilterChain.class)).containsOnlyKeys("cadenaPermisiva");
        }
    }

    @Nested
    @SpringBootTest
    @AutoConfigureMockMvc
    class ConCadenaPropiaComoLaDeC05 {

        @TestConfiguration
        static class SeguridadDeC05 {
            @Bean
            SecurityFilterChain cadenaDeC05(HttpSecurity http) throws Exception {
                return http.authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                        .httpBasic(Customizer.withDefaults())
                        .build();
            }
        }

        @Autowired MockMvc mvc;
        @Autowired ApplicationContext contexto;

        @Test
        void laCadenaPermisivaSeDesactivaYSinCredencialesResponde401() throws Exception {
            assertThat(contexto.getBeansOfType(SecurityFilterChain.class)).containsOnlyKeys("cadenaDeC05");
            mvc.perform(get("/test/errores/regla/TURNO_EN_PASADO"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
