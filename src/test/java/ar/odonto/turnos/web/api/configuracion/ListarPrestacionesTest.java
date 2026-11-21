package ar.odonto.turnos.web.api.configuracion;

import ar.odonto.turnos.domain.model.Prestacion;
import ar.odonto.turnos.support.DatosDePrueba;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Listado de prestaciones, activas e inactivas (spec: configuracion-consultorio). */
class ListarPrestacionesTest extends ConfiguracionApiTestBase {

    @Test
    void listadoVacio() throws Exception {
        getJson("/api/prestaciones")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void listadoConPrestaciones() throws Exception {
        Prestacion control = prestaciones.save(DatosDePrueba.prestacion("Control", 30));
        Prestacion limpieza = prestaciones.save(DatosDePrueba.prestacion("Limpieza", 45));
        limpieza.desactivar();

        getJson("/api/prestaciones")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(control.getId()))
                .andExpect(jsonPath("$[1].activa").value(false));
    }
}
