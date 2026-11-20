package ar.odonto.turnos.domain.model;

import ar.odonto.turnos.domain.error.CodigoError;
import ar.odonto.turnos.domain.error.ReglaDeNegocioException;
import ar.odonto.turnos.support.DatosDePrueba;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Invariantes de datos que protege el modelo (tarea 4.1 de C-01). Las reglas de negocio se testean en cada change.
 */
class InvariantesDelModeloTest {

    private final Consultorio consultorio = DatosDePrueba.consultorio();

    @Test
    void horario_conAperturaIgualOPosteriorAlCierre_esRechazado() {
        assertThatThrownBy(() -> new HorarioAtencion(consultorio, DayOfWeek.MONDAY, LocalTime.of(20, 0), LocalTime.of(8, 0)))
                .isInstanceOf(ReglaDeNegocioException.class)
                .extracting(ex -> ((ReglaDeNegocioException) ex).codigo()).isEqualTo(CodigoError.HORARIO_INVALIDO);
        assertThatThrownBy(() -> new HorarioAtencion(consultorio, DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(8, 0)))
                .extracting(ex -> ((ReglaDeNegocioException) ex).codigo()).isEqualTo(CodigoError.HORARIO_INVALIDO);
    }

    @Test
    void horario_valido_seCrea() {
        HorarioAtencion horario = DatosDePrueba.horario(consultorio, DayOfWeek.MONDAY);
        assertThat(horario.getApertura()).isBefore(horario.getCierre());
    }

    @ParameterizedTest(name = "duración {0} min rechazada")
    @ValueSource(ints = {0, -5, 7, 33})
    void prestacion_conDuracionInvalida_esRechazada(int minutos) {
        assertThatThrownBy(() -> new Prestacion("Control", minutos))
                .extracting(ex -> ((ReglaDeNegocioException) ex).codigo()).isEqualTo(CodigoError.DURACION_INVALIDA);
    }

    @ParameterizedTest(name = "duración {0} min aceptada")
    @ValueSource(ints = {5, 30, 45, 60})
    void prestacion_conDuracionMultiploDe5_seAcepta(int minutos) {
        assertThat(new Prestacion("Control", minutos).duracion().toMinutes()).isEqualTo(minutos);
    }

    @Test
    void turno_conFinNoPosteriorAlInicio_esRechazado() {
        assertThatThrownBy(() -> DatosDePrueba.turno(DatosDePrueba.profesional("MP-1"), DatosDePrueba.sillon("S1"),
                DatosDePrueba.paciente(1), DatosDePrueba.prestacion("Control", 30),
                "2026-11-23T10:00", "2026-11-23T10:00"))
                .extracting(ex -> ((ReglaDeNegocioException) ex).codigo()).isEqualTo(CodigoError.ENTRADA_INVALIDA);
    }

    @Test
    void turno_sinSillon_esRechazadoConSillonRequerido() {
        assertThatThrownBy(() -> new Turno(DatosDePrueba.profesional("MP-1"), null, DatosDePrueba.paciente(1),
                DatosDePrueba.prestacion("Control", 30), LocalDateTime.parse("2026-11-23T10:00"),
                LocalDateTime.parse("2026-11-23T10:30"), false, LocalDateTime.parse("2026-11-20T10:00"), "prueba"))
                .extracting(ex -> ((ReglaDeNegocioException) ex).codigo()).isEqualTo(CodigoError.SILLON_REQUERIDO);
    }

    @Test
    void turno_nuevo_estaReservado_yCancelarloDosVecesFalla() {
        Turno turno = DatosDePrueba.turno(DatosDePrueba.profesional("MP-1"), DatosDePrueba.sillon("S1"),
                DatosDePrueba.paciente(1), DatosDePrueba.prestacion("Control", 30),
                "2026-11-23T10:00", "2026-11-23T10:30");
        assertThat(turno.estaActivo()).isTrue();

        turno.marcarCancelado(LocalDateTime.parse("2026-11-22T10:00"));
        assertThat(turno.getEstado()).isEqualTo(TurnoEstado.CANCELADO);
        assertThat(turno.getCanceladoEn()).isEqualTo(LocalDateTime.parse("2026-11-22T10:00"));

        assertThatThrownBy(() -> turno.marcarCancelado(LocalDateTime.parse("2026-11-22T11:00")))
                .extracting(ex -> ((ReglaDeNegocioException) ex).codigo()).isEqualTo(CodigoError.TURNO_YA_CANCELADO);
    }

    @Test
    void usuarioOdontologo_sinProfesional_esRechazado() {
        assertThatThrownBy(() -> new Usuario("ana", "hash", Rol.ODONTOLOGO, null))
                .extracting(ex -> ((ReglaDeNegocioException) ex).codigo()).isEqualTo(CodigoError.ENTRADA_INVALIDA);
    }

    @Test
    void paciente_toString_noExponeDatosPersonales() {
        assertThat(DatosDePrueba.paciente(1).toString()).doesNotContain("00000001").doesNotContain("Paciente Numero");
    }
}
