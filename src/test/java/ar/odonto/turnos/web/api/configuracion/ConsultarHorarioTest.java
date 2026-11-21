package ar.odonto.turnos.web.api.configuracion;

import ar.odonto.turnos.domain.model.Consultorio;
import ar.odonto.turnos.support.DatosDePrueba;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Lectura de la franja de los siete días (spec: configuracion-consultorio). */
class ConsultarHorarioTest extends ConfiguracionApiTestBase {

    @Test
    void sieteDiasCerrados() throws Exception {
        getJson("/api/consultorio/horario")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(7)))
                .andExpect(jsonPath("$[0].diaSemana").value("MONDAY"))
                .andExpect(jsonPath("$[0].apertura").value(nullValue()))
                .andExpect(jsonPath("$[0].cierre").value(nullValue()))
                .andExpect(jsonPath("$[6].diaSemana").value("SUNDAY"))
                .andExpect(jsonPath("$[6].apertura").value(nullValue()))
                .andExpect(jsonPath("$[6].cierre").value(nullValue()));
    }

    @Test
    void lunesConFranja() throws Exception {
        Consultorio consultorio = consultorios.save(DatosDePrueba.consultorio());
        horarios.save(DatosDePrueba.horario(consultorio, DayOfWeek.MONDAY));

        getJson("/api/consultorio/horario")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(7)))
                .andExpect(jsonPath("$[0].diaSemana").value("MONDAY"))
                .andExpect(jsonPath("$[0].apertura").value("08:00"))
                .andExpect(jsonPath("$[0].cierre").value("20:00"))
                .andExpect(jsonPath("$[1].diaSemana").value("TUESDAY"))
                .andExpect(jsonPath("$[1].apertura").value(nullValue()));
    }

    @Test
    void martesConFranjaDistinta() throws Exception {
        Consultorio consultorio = consultorios.save(DatosDePrueba.consultorio());
        horarios.save(new ar.odonto.turnos.domain.model.HorarioAtencion(consultorio, DayOfWeek.TUESDAY,
                java.time.LocalTime.of(9, 0), java.time.LocalTime.of(18, 0)));

        getJson("/api/consultorio/horario")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(7)))
                .andExpect(jsonPath("$[0].apertura").value(nullValue()))
                .andExpect(jsonPath("$[1].apertura").value("09:00"))
                .andExpect(jsonPath("$[1].cierre").value("18:00"));
    }
}
