package ar.odonto.turnos.web.api.configuracion;

import ar.odonto.turnos.domain.model.Paciente;
import ar.odonto.turnos.domain.model.Prestacion;
import ar.odonto.turnos.domain.model.Profesional;
import ar.odonto.turnos.domain.model.Sillon;
import ar.odonto.turnos.domain.model.Turno;
import ar.odonto.turnos.support.DatosDePrueba;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Baja lógica y reactivación sin borrar recursos ni turnos (spec: configuracion-consultorio). */
class DesactivarSillonTest extends ConfiguracionApiTestBase {

    Sillon sillon;
    Turno turno;

    @BeforeEach
    void contextoConTurno() {
        Profesional profesional = profesionales.save(DatosDePrueba.profesional("M-0010"));
        sillon = sillones.save(DatosDePrueba.sillon("Sillon 1"));
        Paciente paciente = pacientes.save(DatosDePrueba.paciente(1));
        Prestacion prestacion = prestaciones.save(DatosDePrueba.prestacion("Control", 30));
        turno = turnos.save(DatosDePrueba.turno(profesional, sillon, paciente, prestacion,
                "2026-11-23T09:30", "2026-11-23T10:00"));
    }

    @Test
    void desactivarConTurnoConservaTurno() throws Exception {
        putJson("/api/sillones/" + sillon.getId(), """
                {"activo":false}
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));

        getJson("/api/turnos/" + turno.getId())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sillonId").value(sillon.getId()))
                .andExpect(jsonPath("$.estado").value("RESERVADO"));

        getJson("/api/sillones")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(sillon.getId()))
                .andExpect(jsonPath("$[0].activo").value(false));
        assertThat(sillones.count()).isEqualTo(1);
        assertThat(turnos.count()).isEqualTo(1);
    }

    @Test
    void reactivarSillon() throws Exception {
        putJson("/api/sillones/" + sillon.getId(), """
                {"activo":false}
                """).andExpect(status().isOk());

        putJson("/api/sillones/" + sillon.getId(), """
                {"activo":true}
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(true));

        getJson("/api/sillones").andExpect(jsonPath("$[0].activo").value(true));
    }

    @Test
    void desactivarProfesionalYPrestacionConTurno() throws Exception {
        Long profesionalId = turno.getProfesional().getId();
        Long prestacionId = turno.getPrestacion().getId();

        putJson("/api/profesionales/" + profesionalId, """
                {"activo":false}
                """).andExpect(status().isOk()).andExpect(jsonPath("$.activo").value(false));
        putJson("/api/prestaciones/" + prestacionId, """
                {"activa":false}
                """).andExpect(status().isOk()).andExpect(jsonPath("$.activa").value(false));

        getJson("/api/turnos/" + turno.getId())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RESERVADO"));
        assertThat(profesionales.findById(profesionalId).orElseThrow().isActivo()).isFalse();
        assertThat(prestaciones.findById(prestacionId).orElseThrow().isActiva()).isFalse();
    }

    @Test
    void reactivarProfesional() throws Exception {
        Long profesionalId = turno.getProfesional().getId();
        putJson("/api/profesionales/" + profesionalId, """
                {"activo":false}
                """).andExpect(status().isOk());

        putJson("/api/profesionales/" + profesionalId, """
                {"activo":true}
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(true));
    }
}
