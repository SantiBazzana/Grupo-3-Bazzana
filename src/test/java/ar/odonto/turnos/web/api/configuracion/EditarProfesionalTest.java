package ar.odonto.turnos.web.api.configuracion;

import ar.odonto.turnos.domain.model.Profesional;
import ar.odonto.turnos.support.DatosDePrueba;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Escenarios de edición de profesionales (spec: configuracion-consultorio). */
class EditarProfesionalTest extends ConfiguracionApiTestBase {

    @Test
    void editarMismaMatriculaNoDuplicado() throws Exception {
        Profesional profesional = profesionales.save(DatosDePrueba.profesional("M-0001"));

        putJson("/api/profesionales/" + profesional.getId(), """
                {"matricula":"M-0001","activo":false}
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matricula").value("M-0001"))
                .andExpect(jsonPath("$.activo").value(false));

        assertThat(profesionales.findById(profesional.getId()).orElseThrow().isActivo()).isFalse();
    }

    @Test
    void matriculaInmutable() throws Exception {
        Profesional profesional = profesionales.save(DatosDePrueba.profesional("M-0001"));

        codigo(putJson("/api/profesionales/" + profesional.getId(), """
                {"matricula":"M-0099","nombre":"Otro"}
                """), 400, "ENTRADA_INVALIDA");

        assertThat(profesionales.findById(profesional.getId()).orElseThrow().getMatricula()).isEqualTo("M-0001");
    }

    @Test
    void otraMatriculaDistintaTambienRechazada() throws Exception {
        Profesional profesional = profesionales.save(DatosDePrueba.profesional("M-0003"));

        codigo(putJson("/api/profesionales/" + profesional.getId(), """
                {"matricula":"M-0099"}
                """), 400, "ENTRADA_INVALIDA");

        assertThat(profesionales.findById(profesional.getId()).orElseThrow().getMatricula()).isEqualTo("M-0003");
    }

    @Test
    void editarSoloTelefonoYActivo() throws Exception {
        Profesional profesional = profesionales.save(DatosDePrueba.profesional("M-0004"));
        profesional.desactivar();

        putJson("/api/profesionales/" + profesional.getId(), """
                {"telefono":"5491100000009","activo":true}
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matricula").value("M-0004"))
                .andExpect(jsonPath("$.telefono").value("5491100000009"))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @ParameterizedTest(name = "nombre en blanco rechazado: [{0}]")
    @ValueSource(strings = {"   ", ""})
    void editarConNombreEnBlanco(String nombre) throws Exception {
        Profesional profesional = profesionales.save(DatosDePrueba.profesional("M-0002"));

        nombreEnBlancoRechazado("/api/profesionales", profesional.getId(), nombre);

        assertThat(profesionales.findById(profesional.getId()).orElseThrow().getNombre()).isEqualTo("Ana");
    }

    @Test
    void editarConNombreNullEsEdicionParcial() throws Exception {
        Profesional profesional = profesionales.save(DatosDePrueba.profesional("M-0006"));

        putJson("/api/profesionales/" + profesional.getId(), """
                {"nombre":null,"telefono":"5491100000011"}
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Ana"))
                .andExpect(jsonPath("$.telefono").value("5491100000011"));
    }

    @Test
    void editarConNombreValidoConEspaciosInternos() throws Exception {
        Profesional profesional = profesionales.save(DatosDePrueba.profesional("M-0007"));

        putJson("/api/profesionales/" + profesional.getId(), """
                {"nombre":"Ana Maria","apellido":"Ejemplo Perez"}
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Ana Maria"))
                .andExpect(jsonPath("$.apellido").value("Ejemplo Perez"));
    }

    @Test
    void editarInexistente() throws Exception {
        codigo(putJson("/api/profesionales/999999", """
                {"activo":false}
                """), 404, "RECURSO_INEXISTENTE");
    }

    @Test
    void otroProfesionalInexistente() throws Exception {
        codigo(putJson("/api/profesionales/999998", """
                {"matricula":"M-0005"}
                """), 404, "RECURSO_INEXISTENTE");
    }
}
