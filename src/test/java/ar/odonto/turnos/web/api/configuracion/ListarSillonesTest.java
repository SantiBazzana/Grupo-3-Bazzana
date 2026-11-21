package ar.odonto.turnos.web.api.configuracion;

import ar.odonto.turnos.domain.model.Sillon;
import ar.odonto.turnos.support.DatosDePrueba;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Listado de sillones activos e inactivos (spec: configuracion-consultorio). */
class ListarSillonesTest extends ConfiguracionApiTestBase {

    @Test
    void listadoIncluyeInactivos() throws Exception {
        Sillon activo = sillones.save(DatosDePrueba.sillon("Sillon 1"));
        Sillon inactivo = sillones.save(DatosDePrueba.sillon("Sillon 2"));
        inactivo.desactivar();

        getJson("/api/sillones")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(activo.getId()))
                .andExpect(jsonPath("$[0].activo").value(true))
                .andExpect(jsonPath("$[1].id").value(inactivo.getId()))
                .andExpect(jsonPath("$[1].activo").value(false));
    }

    @Test
    void listadoConUnSoloSillon() throws Exception {
        sillones.save(DatosDePrueba.sillon("Sillon 3"));

        getJson("/api/sillones")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre").value("Sillon 3"));
    }
}
