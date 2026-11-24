package ar.odonto.turnos.web.view.gestion;

import ar.odonto.turnos.domain.model.Rol;
import ar.odonto.turnos.domain.model.Usuario;
import ar.odonto.turnos.domain.port.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Escenarios del spec (uno por test): feliz (el form de login con token CSRF procesa el POST y
 * redirige al panel) y borde (POST sin token CSRF rechazado; credenciales inválidas).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LoginVistaTest {

    private static final String USUARIO = "usuario.login";
    private static final String CLAVE = "clave-ficticia";

    @Autowired MockMvc mvc;
    @Autowired UsuarioRepository usuarios;
    @Autowired PasswordEncoder passwordEncoder;

    @BeforeEach
    void semilla() {
        if (!usuarios.existsByUsername(USUARIO)) {
            usuarios.save(new Usuario(USUARIO, passwordEncoder.encode(CLAVE), Rol.SECRETARIA, null));
        }
    }

    @Test
    void vistaLoginMuestraElFormularioConTokenCsrf() throws Exception {
        String html = mvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andReturn().getResponse().getContentAsString();

        assertThat(html)
                .contains("action=\"/login\"")
                .contains("method=\"post\"")
                .contains("name=\"_csrf\"")
                .contains("name=\"username\"")
                .contains("name=\"password\"");
    }

    @Test
    void loginExitosoConTokenCsrfRedirigeAlPanel() throws Exception {
        mvc.perform(formLogin("/login").user(USUARIO).password(CLAVE))
                .andExpect(authenticated().withUsername(USUARIO))
                .andExpect(redirectedUrl("/panel"));
    }

    @Test
    void credencialesInvalidasRedirigenAlFormularioConError() throws Exception {
        mvc.perform(formLogin("/login").user(USUARIO).password("incorrecta"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void postDeLoginSinTokenCsrfEsRechazado() throws Exception {
        mvc.perform(post("/login")
                        .param("username", USUARIO)
                        .param("password", CLAVE))
                .andExpect(status().isForbidden());
    }
}
