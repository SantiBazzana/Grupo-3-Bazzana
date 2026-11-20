package ar.odonto.turnos.support;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Spec base-del-sistema: "Zona horaria y reloj del sistema", escenario "Reloj fijo para verificación".
 */
class RelojDePruebaTest {

    @Test
    void relojFijo_cualquierConsultaDelAhoraDevuelveEseInstante() throws InterruptedException {
        Clock reloj = RelojDePrueba.en("2026-11-23T09:00");

        LocalDateTime primera = LocalDateTime.now(reloj);
        Thread.sleep(5);
        LocalDateTime segunda = LocalDateTime.now(reloj);

        assertThat(primera).isEqualTo(LocalDateTime.of(2026, 11, 23, 9, 0));
        assertThat(segunda).isEqualTo(primera);
    }
}
