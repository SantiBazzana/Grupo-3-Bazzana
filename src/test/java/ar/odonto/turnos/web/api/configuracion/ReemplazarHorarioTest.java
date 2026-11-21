package ar.odonto.turnos.web.api.configuracion;

import ar.odonto.turnos.domain.model.Consultorio;
import ar.odonto.turnos.support.DatosDePrueba;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Reemplazo y cierre de la franja diaria (spec: configuracion-consultorio, decisiones a y b). */
class ReemplazarHorarioTest extends ConfiguracionApiTestBase {

    @Test
    void configurarLunes() throws Exception {
        consultorios.save(DatosDePrueba.consultorio());

        putJson("/api/consultorio/horario", """
                {"diaSemana":"MONDAY","apertura":"08:00","cierre":"20:00"}
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diaSemana").value("MONDAY"))
                .andExpect(jsonPath("$.apertura").value("08:00"))
                .andExpect(jsonPath("$.cierre").value("20:00"));

        getJson("/api/consultorio/horario")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].apertura").value("08:00"))
                .andExpect(jsonPath("$[0].cierre").value("20:00"));
        assertThat(horarios.count()).isEqualTo(1);
    }

    @Test
    void configurarMiercoles() throws Exception {
        consultorios.save(DatosDePrueba.consultorio());

        putJson("/api/consultorio/horario", """
                {"diaSemana":"WEDNESDAY","apertura":"09:30","cierre":"18:30"}
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diaSemana").value("WEDNESDAY"))
                .andExpect(jsonPath("$.apertura").value("09:30"))
                .andExpect(jsonPath("$.cierre").value("18:30"));

        getJson("/api/consultorio/horario")
                .andExpect(jsonPath("$[2].apertura").value("09:30"));
    }

    @Test
    void reemplazarLunes() throws Exception {
        Consultorio consultorio = consultorios.save(DatosDePrueba.consultorio());
        horarios.save(DatosDePrueba.horario(consultorio, DayOfWeek.MONDAY));

        putJson("/api/consultorio/horario", """
                {"diaSemana":"MONDAY","apertura":"09:00","cierre":"18:00"}
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apertura").value("09:00"))
                .andExpect(jsonPath("$.cierre").value("18:00"));

        getJson("/api/consultorio/horario")
                .andExpect(jsonPath("$[0].apertura").value("09:00"))
                .andExpect(jsonPath("$[0].cierre").value("18:00"));
        assertThat(horarios.count()).isEqualTo(1);
    }

    @Test
    void reemplazarConOtroPar() throws Exception {
        Consultorio consultorio = consultorios.save(DatosDePrueba.consultorio());
        horarios.save(DatosDePrueba.horario(consultorio, DayOfWeek.FRIDAY));

        putJson("/api/consultorio/horario", """
                {"diaSemana":"FRIDAY","apertura":"10:00","cierre":"16:00"}
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apertura").value("10:00"));

        assertThat(horarios.findAll()).singleElement()
                .satisfies(franja -> assertThat(franja.getCierre()).isEqualTo(java.time.LocalTime.of(16, 0)));
    }

    @Test
    void sinConsultorio() throws Exception {
        codigo(putJson("/api/consultorio/horario", """
                {"diaSemana":"MONDAY","apertura":"08:00","cierre":"20:00"}
                """), 404, "RECURSO_INEXISTENTE");

        getJson("/api/consultorio/horario")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(7)))
                .andExpect(jsonPath("$[0].apertura").value(nullValue()));
        assertThat(horarios.count()).isZero();
    }

    @Test
    void aperturaIgualCierre() throws Exception {
        consultorios.save(DatosDePrueba.consultorio());

        codigo(putJson("/api/consultorio/horario", """
                {"diaSemana":"MONDAY","apertura":"10:00","cierre":"10:00"}
                """), 422, "HORARIO_INVALIDO");

        assertThat(horarios.count()).isZero();
    }

    @Test
    void otraAperturaIgualCierre() throws Exception {
        consultorios.save(DatosDePrueba.consultorio());

        codigo(putJson("/api/consultorio/horario", """
                {"diaSemana":"TUESDAY","apertura":"12:30","cierre":"12:30"}
                """), 422, "HORARIO_INVALIDO");
    }

    @Test
    void aperturaPosteriorCierre() throws Exception {
        consultorios.save(DatosDePrueba.consultorio());

        codigo(putJson("/api/consultorio/horario", """
                {"diaSemana":"MONDAY","apertura":"20:00","cierre":"08:00"}
                """), 422, "HORARIO_INVALIDO");

        assertThat(horarios.count()).isZero();
    }

    @Test
    void medianocheInvalida() throws Exception {
        consultorios.save(DatosDePrueba.consultorio());

        codigo(putJson("/api/consultorio/horario", """
                {"diaSemana":"SUNDAY","apertura":"00:00","cierre":"00:00"}
                """), 422, "HORARIO_INVALIDO");
    }

    @Test
    void cerrarDiaConFranja() throws Exception {
        Consultorio consultorio = consultorios.save(DatosDePrueba.consultorio());
        horarios.save(DatosDePrueba.horario(consultorio, DayOfWeek.MONDAY));
        horarios.save(DatosDePrueba.horario(consultorio, DayOfWeek.TUESDAY));

        putJson("/api/consultorio/horario", """
                {"diaSemana":"MONDAY","apertura":null,"cierre":null}
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diaSemana").value("MONDAY"))
                .andExpect(jsonPath("$.apertura").value(nullValue()))
                .andExpect(jsonPath("$.cierre").value(nullValue()));

        getJson("/api/consultorio/horario")
                .andExpect(jsonPath("$[0].apertura").value(nullValue()))
                .andExpect(jsonPath("$[0].cierre").value(nullValue()))
                .andExpect(jsonPath("$[1].apertura").value("08:00"));
        assertThat(horarios.findAll()).singleElement()
                .satisfies(franja -> assertThat(franja.getDiaSemana()).isEqualTo(DayOfWeek.TUESDAY));
    }

    @Test
    void cerrarOtroDiaConservaLosDemas() throws Exception {
        Consultorio consultorio = consultorios.save(DatosDePrueba.consultorio());
        horarios.save(DatosDePrueba.horario(consultorio, DayOfWeek.WEDNESDAY));
        horarios.save(DatosDePrueba.horario(consultorio, DayOfWeek.THURSDAY));

        putJson("/api/consultorio/horario", """
                {"diaSemana":"WEDNESDAY","apertura":null,"cierre":null}
                """).andExpect(status().isOk());

        assertThat(horarios.findAll()).singleElement()
                .satisfies(franja -> assertThat(franja.getDiaSemana()).isEqualTo(DayOfWeek.THURSDAY));
    }

    @Test
    void soloApertura() throws Exception {
        Consultorio consultorio = consultorios.save(DatosDePrueba.consultorio());
        horarios.save(DatosDePrueba.horario(consultorio, DayOfWeek.MONDAY));

        codigo(putJson("/api/consultorio/horario", """
                {"diaSemana":"MONDAY","apertura":"08:00","cierre":null}
                """), 400, "ENTRADA_INVALIDA");

        getJson("/api/consultorio/horario")
                .andExpect(jsonPath("$[0].apertura").value("08:00"));
        assertThat(horarios.count()).isEqualTo(1);
    }

    @Test
    void soloCierre() throws Exception {
        consultorios.save(DatosDePrueba.consultorio());

        codigo(putJson("/api/consultorio/horario", """
                {"diaSemana":"FRIDAY","apertura":null,"cierre":"20:00"}
                """), 400, "ENTRADA_INVALIDA");

        assertThat(horarios.count()).isZero();
    }
}
