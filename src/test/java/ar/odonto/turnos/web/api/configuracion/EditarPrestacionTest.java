package ar.odonto.turnos.web.api.configuracion;

import ar.odonto.turnos.domain.model.Prestacion;
import ar.odonto.turnos.support.DatosDePrueba;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Escenarios de edición de prestaciones (spec: configuracion-consultorio). */
class EditarPrestacionTest extends ConfiguracionApiTestBase {

    @Test
    void editarDuracionYDesactivar() throws Exception {
        Prestacion control = prestaciones.save(DatosDePrueba.prestacion("Control", 30));

        putJson("/api/prestaciones/" + control.getId(), """
                {"nombre":"Control","duracionMinutos":45,"activa":false}
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(control.getId()))
                .andExpect(jsonPath("$.nombre").value("Control"))
                .andExpect(jsonPath("$.duracionMinutos").value(45))
                .andExpect(jsonPath("$.activa").value(false));

        assertThat(prestaciones.findById(control.getId()).orElseThrow().getDuracionMinutos()).isEqualTo(45);
        assertThat(prestaciones.findById(control.getId()).orElseThrow().isActiva()).isFalse();
    }

    @Test
    void reactivarPrestacion() throws Exception {
        Prestacion control = prestaciones.save(DatosDePrueba.prestacion("Control", 30));
        control.desactivar();

        putJson("/api/prestaciones/" + control.getId(), """
                {"activa":true}
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duracionMinutos").value(30))
                .andExpect(jsonPath("$.activa").value(true));

        assertThat(prestaciones.findById(control.getId()).orElseThrow().isActiva()).isTrue();
    }

    @Test
    void editarConDuracion7() throws Exception {
        Prestacion control = prestaciones.save(DatosDePrueba.prestacion("Control", 30));

        codigo(putJson("/api/prestaciones/" + control.getId(), """
                {"duracionMinutos":7}
                """), 422, "DURACION_INVALIDA");

        assertThat(prestaciones.findById(control.getId()).orElseThrow().getDuracionMinutos()).isEqualTo(30);
    }

    @Test
    void editarConDuracionCero() throws Exception {
        Prestacion control = prestaciones.save(DatosDePrueba.prestacion("Control", 30));

        codigo(putJson("/api/prestaciones/" + control.getId(), """
                {"duracionMinutos":0}
                """), 422, "DURACION_INVALIDA");

        assertThat(prestaciones.findById(control.getId()).orElseThrow().getDuracionMinutos()).isEqualTo(30);
    }

    @Test
    void editarInexistente() throws Exception {
        codigo(putJson("/api/prestaciones/999999", """
                {"duracionMinutos":45}
                """), 404, "RECURSO_INEXISTENTE");
    }

    @Test
    void editarOtroIdInexistente() throws Exception {
        codigo(putJson("/api/prestaciones/999998", """
                {"activa":false}
                """), 404, "RECURSO_INEXISTENTE");
    }
}
