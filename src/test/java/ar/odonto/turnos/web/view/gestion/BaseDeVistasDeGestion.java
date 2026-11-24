package ar.odonto.turnos.web.view.gestion;

import ar.odonto.turnos.domain.port.ConsultorioRepository;
import ar.odonto.turnos.domain.port.HorarioAtencionRepository;
import ar.odonto.turnos.domain.port.PacienteRepository;
import ar.odonto.turnos.domain.port.PrestacionRepository;
import ar.odonto.turnos.domain.port.ProfesionalRepository;
import ar.odonto.turnos.domain.port.SillonRepository;
import ar.odonto.turnos.domain.port.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Base de tests de vistas de gestión: contexto real H2 + MockMvc; cada test revierte sus datos. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
abstract class BaseDeVistasDeGestion {

    @Autowired MockMvc mvc;
    @Autowired ProfesionalRepository profesionales;
    @Autowired SillonRepository sillones;
    @Autowired PrestacionRepository prestaciones;
    @Autowired PacienteRepository pacientes;
    @Autowired ConsultorioRepository consultorios;
    @Autowired HorarioAtencionRepository horarios;
    @Autowired UsuarioRepository usuarios;
    @Autowired PasswordEncoder passwordEncoder;

    MockHttpServletRequestBuilder postConCsrf(String ruta, String... parametros) {
        MockHttpServletRequestBuilder peticion = post(ruta).with(csrf());
        for (int i = 0; i + 1 < parametros.length; i += 2) {
            peticion.param(parametros[i], parametros[i + 1]);
        }
        return peticion;
    }

    String htmlDe(String ruta) throws Exception {
        MvcResult resultado = mvc.perform(get(ruta))
                .andExpect(status().isOk())
                .andReturn();
        return resultado.getResponse().getContentAsString();
    }
}
