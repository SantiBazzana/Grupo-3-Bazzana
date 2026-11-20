package ar.odonto.turnos.domain.rules.turnos;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Tarea 1.1 de C-02: margen y superposición (incluye el borde abstracto de 4 minutos, design D7). */
class IntervaloTest {

    private static final Duration MARGEN = Duration.ofMinutes(5);
    private static final Intervalo EXISTENTE = intervalo("09:30", "10:00");

    static Intervalo intervalo(String inicio, String fin) {
        return new Intervalo(LocalDateTime.parse("2026-11-23T" + inicio), LocalDateTime.parse("2026-11-23T" + fin));
    }

    @Test
    void empiezaCincoMinutosDespues_noChoca() {
        assertThat(intervalo("10:05", "10:35").chocaConMargen(EXISTENTE, MARGEN)).isFalse();
    }

    @Test
    void empiezaCuatroMinutosDespues_choca() {
        assertThat(intervalo("10:04", "10:34").chocaConMargen(EXISTENTE, MARGEN)).isTrue();
    }

    @Test
    void terminaCincoMinutosAntes_noChoca_yCuatroAntes_choca() {
        assertThat(intervalo("08:55", "09:25").chocaConMargen(EXISTENTE, MARGEN)).isFalse();
        assertThat(intervalo("08:56", "09:26").chocaConMargen(EXISTENTE, MARGEN)).isTrue();
    }

    @Test
    void elChoqueEsSimetrico() {
        Intervalo nuevo = intervalo("10:04", "10:34");
        assertThat(EXISTENTE.chocaConMargen(nuevo, MARGEN)).isEqualTo(nuevo.chocaConMargen(EXISTENTE, MARGEN));
    }

    @Test
    void superposicionSinMargen_encadenadoNoSeSuperpone() {
        assertThat(intervalo("10:00", "10:30").seSuperpone(EXISTENTE)).isFalse();
        assertThat(intervalo("09:59", "10:29").seSuperpone(EXISTENTE)).isTrue();
    }

    @Test
    void intervaloLejano_noChoca() {
        assertThat(intervalo("15:00", "15:30").chocaConMargen(EXISTENTE, MARGEN)).isFalse();
    }

    @Test
    void finNoPosteriorAlInicio_esInvalido() {
        assertThatThrownBy(() -> intervalo("10:00", "10:00")).isInstanceOf(IllegalArgumentException.class);
    }
}
