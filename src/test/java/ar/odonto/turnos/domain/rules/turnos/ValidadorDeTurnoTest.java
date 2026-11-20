package ar.odonto.turnos.domain.rules.turnos;

import ar.odonto.turnos.domain.error.CodigoError;
import ar.odonto.turnos.domain.error.ReglaDeNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tarea 1.2 de C-02: cada regla RN-TU en Java puro, sin Spring ni base de datos.
 * Contexto de la spec: ahora = lunes 2026-11-23 08:00, franja 08:00-20:00, Control 30 min, margen 5 min.
 */
class ValidadorDeTurnoTest {

    private static final LocalDateTime AHORA = LocalDateTime.parse("2026-11-23T08:00");
    private static final ContextoDeTurno.Franja FRANJA = new ContextoDeTurno.Franja(LocalTime.of(8, 0), LocalTime.of(20, 0));
    private static final Intervalo EXISTENTE = intervalo("2026-11-23T09:30", 30);

    static Intervalo intervalo(String inicio, int minutos) {
        LocalDateTime i = LocalDateTime.parse(inicio);
        return new Intervalo(i, i.plusMinutes(minutos));
    }

    /** Builder mínimo: por defecto, todo válido y sin candidatos. */
    static final class Ctx {
        LocalDateTime ahora = AHORA;
        Intervalo turno = intervalo("2026-11-23T10:00", 30);
        boolean sobreturno;
        ContextoDeTurno.Franja franja = FRANJA;
        boolean sillonActivo = true;
        boolean profesionalActivo = true;
        boolean prestacionActiva = true;
        List<Intervalo> sillon = List.of();
        List<Intervalo> profesional = List.of();
        List<Intervalo> paciente = List.of();

        Ctx turno(String inicio) { this.turno = intervalo(inicio, 30); return this; }
        Ctx sobreturno() { this.sobreturno = true; return this; }

        ContextoDeTurno build() {
            return new ContextoDeTurno(ahora, turno, sobreturno, franja, sillonActivo, profesionalActivo,
                    prestacionActiva, Duration.ofMinutes(5), sillon, profesional, paciente);
        }
    }

    static void rechaza(Ctx ctx, CodigoError codigo) {
        assertThatThrownBy(() -> ValidadorDeTurno.validar(ctx.build()))
                .isInstanceOf(ReglaDeNegocioException.class)
                .extracting(ex -> ((ReglaDeNegocioException) ex).codigo()).isEqualTo(codigo);
    }

    static void acepta(Ctx ctx) {
        assertThatCode(() -> ValidadorDeTurno.validar(ctx.build())).doesNotThrowAnyException();
    }

    @Test
    void turnoLibre_seAcepta() {
        acepta(new Ctx());
    }

    // RN-TU-08
    @Test
    void recursoInactivo_rechazaConRecursoInactivo() {
        Ctx ctx = new Ctx();
        ctx.sillonActivo = false;
        rechaza(ctx, CodigoError.RECURSO_INACTIVO);
        ctx = new Ctx();
        ctx.profesionalActivo = false;
        rechaza(ctx, CodigoError.RECURSO_INACTIVO);
        ctx = new Ctx();
        ctx.prestacionActiva = false;
        rechaza(ctx, CodigoError.RECURSO_INACTIVO);
    }

    // RN-TU-11
    @ParameterizedTest(name = "inicio {0} rechazado")
    @ValueSource(strings = {"2026-11-23T09:03", "2026-11-23T09:05:30"})
    void inicioNoMultiploDe5_oConSegundos_rechazaConEntradaInvalida(String inicio) {
        rechaza(new Ctx().turno(inicio), CodigoError.ENTRADA_INVALIDA);
    }

    // RN-TU-06
    @Test
    void turnoEnElPasado_yEnElAhoraExacto_rechazanConTurnoEnPasado() {
        rechaza(new Ctx().turno("2026-11-20T10:00"), CodigoError.TURNO_EN_PASADO);
        rechaza(new Ctx().turno("2026-11-23T08:00"), CodigoError.TURNO_EN_PASADO);
    }

    // RN-TU-07
    @Test
    void horario_terminaDespuesDelCierre_rechaza_yJustoAlCierre_acepta() {
        rechaza(new Ctx().turno("2026-11-23T19:45"), CodigoError.FUERA_DE_HORARIO);
        acepta(new Ctx().turno("2026-11-23T19:30"));
    }

    @Test
    void horario_empiezaAntesDeLaApertura_rechaza() {
        rechaza(new Ctx().turno("2026-11-24T07:45"), CodigoError.FUERA_DE_HORARIO);
    }

    @Test
    void horario_diaSinFranja_rechaza() {
        Ctx ctx = new Ctx().turno("2026-11-28T10:00");
        ctx.franja = null;
        rechaza(ctx, CodigoError.FUERA_DE_HORARIO);
    }

    // RN-TU-03
    @Test
    void sillon_ocupado_rechaza_inclusoComoSobreturno() {
        Ctx ctx = new Ctx().turno("2026-11-23T09:45");
        ctx.sillon = List.of(EXISTENTE);
        rechaza(ctx, CodigoError.CHOQUE_SILLON);
        rechaza(ctx.sobreturno(), CodigoError.CHOQUE_SILLON);
    }

    @Test
    void sillon_bordesDelMargen() {
        Ctx a1005 = new Ctx().turno("2026-11-23T10:05");
        a1005.sillon = List.of(EXISTENTE);
        acepta(a1005);

        Ctx a1000 = new Ctx().turno("2026-11-23T10:00");
        a1000.sillon = List.of(EXISTENTE);
        rechaza(a1000, CodigoError.CHOQUE_SILLON);

        Ctx a0855 = new Ctx().turno("2026-11-23T08:55");
        a0855.sillon = List.of(EXISTENTE);
        acepta(a0855);
    }

    // RN-TU-02 / RN-TU-04
    @Test
    void profesional_ocupado_rechaza() {
        Ctx ctx = new Ctx().turno("2026-11-23T09:45");
        ctx.profesional = List.of(EXISTENTE);
        rechaza(ctx, CodigoError.CHOQUE_PROFESIONAL);
    }

    @Test
    void sobreturno_eximeDelChequeoDelProfesional_margenIncluido() {
        Ctx superpuesto = new Ctx().turno("2026-11-23T09:45").sobreturno();
        superpuesto.profesional = List.of(EXISTENTE);
        acepta(superpuesto);

        Ctx sinMargen = new Ctx().turno("2026-11-23T10:00").sobreturno();
        sinMargen.profesional = List.of(EXISTENTE);
        acepta(sinMargen);
    }

    // RN-TU-05
    @Test
    void paciente_superpuesto_rechaza_yEncadenadoSinMargen_acepta() {
        Ctx superpuesto = new Ctx().turno("2026-11-23T09:45");
        superpuesto.paciente = List.of(EXISTENTE);
        rechaza(superpuesto, CodigoError.CHOQUE_PACIENTE);

        Ctx encadenado = new Ctx().turno("2026-11-23T10:00");
        encadenado.paciente = List.of(EXISTENTE);
        acepta(encadenado);
    }

    // RN-TU-09
    @Test
    void orden_pasadoAntesQueSillon() {
        Ctx ctx = new Ctx().turno("2026-11-23T09:45");
        ctx.ahora = LocalDateTime.parse("2026-11-23T11:00");
        ctx.sillon = List.of(EXISTENTE);
        rechaza(ctx, CodigoError.TURNO_EN_PASADO);
    }

    @Test
    void orden_sillonAntesQueProfesionalYPaciente() {
        Ctx ctx = new Ctx().turno("2026-11-23T09:45");
        ctx.sillon = List.of(EXISTENTE);
        ctx.profesional = List.of(EXISTENTE);
        ctx.paciente = List.of(EXISTENTE);
        rechaza(ctx, CodigoError.CHOQUE_SILLON);
    }

    @Test
    void orden_inicioInvalidoAntesQuePasado() {
        rechaza(new Ctx().turno("2026-11-20T09:03"), CodigoError.ENTRADA_INVALIDA);
    }
}
