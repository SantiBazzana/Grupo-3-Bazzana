package ar.odonto.turnos.web.api.configuracion;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Escenarios de alta de profesionales (spec: configuracion-consultorio). */
class AltaProfesionalTest extends ConfiguracionApiTestBase {

    @Test
    void altaConMatriculaUnica() throws Exception {
        postJson("/api/profesionales", """
                {"nombre":"Ana","apellido":"Ejemplo","matricula":"M-0001","telefono":"5491100000001"}
                """)
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern(".*/api/profesionales/\\d+")))
                .andExpect(jsonPath("$.matricula").value("M-0001"))
                .andExpect(jsonPath("$.telefono").value("5491100000001"))
                .andExpect(jsonPath("$.activo").value(true));

        assertThat(profesionales.count()).isEqualTo(1);
    }

    @Test
    void altaConOtraMatricula() throws Exception {
        postJson("/api/profesionales", """
                {"nombre":"Bruno","apellido":"Ejemplo","matricula":"M-0002","telefono":"5491100000002"}
                """)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.matricula").value("M-0002"));
    }

    @Test
    void matriculaDuplicada() throws Exception {
        profesionales.save(new ar.odonto.turnos.domain.model.Profesional("Ana", "Ejemplo", "M-0001",
                "5491100000001"));

        codigo(postJson("/api/profesionales", """
                {"nombre":"Otra","apellido":"Ejemplo","matricula":"M-0001","telefono":"5491100000002"}
                """), 409, "DATO_DUPLICADO");

        assertThat(profesionales.count()).isEqualTo(1);
    }

    @Test
    void otraMatriculaRepetida() throws Exception {
        profesionales.save(new ar.odonto.turnos.domain.model.Profesional("Bruno", "Ejemplo", "M-0002",
                "5491100000002"));

        codigo(postJson("/api/profesionales", """
                {"nombre":"Otra","apellido":"Ejemplo","matricula":"M-0002","telefono":"5491100000003"}
                """), 409, "DATO_DUPLICADO");

        assertThat(profesionales.count()).isEqualTo(1);
    }
}
