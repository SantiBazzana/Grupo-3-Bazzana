package ar.odonto.turnos.domain.rules.turnos;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Intervalo semiabierto {@code [inicio, fin)} de un turno (knowledge-base/05, Definiciones).
 *
 * @param inicio instante de inicio (incluido)
 * @param fin    instante de fin (excluido), posterior al inicio
 */
public record Intervalo(LocalDateTime inicio, LocalDateTime fin) {

    public Intervalo {
        Objects.requireNonNull(inicio, "inicio");
        Objects.requireNonNull(fin, "fin");
        if (!fin.isAfter(inicio)) {
            throw new IllegalArgumentException("El fin debe ser posterior al inicio");
        }
    }

    /**
     * Choque con margen M (RN-TU-02/03): {@code this.inicio < otro.fin + M} y {@code otro.inicio < this.fin + M}.
     * Con M = 5 min y otro = 09:30-10:00, un intervalo que empieza 10:05 no choca y uno que empieza 10:04 sí.
     */
    public boolean chocaConMargen(Intervalo otro, Duration margen) {
        return inicio.isBefore(otro.fin.plus(margen)) && otro.inicio.isBefore(fin.plus(margen));
    }

    /** Superposición simple, sin margen (RN-TU-05): uno puede empezar exactamente cuando termina el otro. */
    public boolean seSuperpone(Intervalo otro) {
        return chocaConMargen(otro, Duration.ZERO);
    }
}
