package ar.odonto.turnos.domain.rules.turnos;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

/**
 * Todo lo que el {@link ValidadorDeTurno} necesita para decidir, ya cargado por el caso de uso (design D2 de C-02).
 * Es Java puro: se puede armar en un test sin Spring ni base de datos.
 *
 * @param ahora                 instante actual del reloj del sistema (zona del consultorio)
 * @param turno                 intervalo del turno a crear ({@code fin = inicio + duración})
 * @param sobreturno            si se exime del chequeo del profesional (RN-TU-04)
 * @param franja                franja de atención del día; {@code null} si el día no tiene atención o no hay consultorio
 * @param sillonActivo          RN-TU-08
 * @param profesionalActivo     RN-TU-08
 * @param prestacionActiva      RN-TU-08
 * @param margen                margen entre turnos del mismo profesional o sillón (RN-TU-02/03)
 * @param candidatosSillon      intervalos de turnos activos del mismo sillón cercanos al turno
 * @param candidatosProfesional intervalos de turnos activos del mismo profesional cercanos al turno
 * @param candidatosPaciente    intervalos de turnos activos del mismo paciente cercanos al turno
 */
public record ContextoDeTurno(
        LocalDateTime ahora,
        Intervalo turno,
        boolean sobreturno,
        Franja franja,
        boolean sillonActivo,
        boolean profesionalActivo,
        boolean prestacionActiva,
        Duration margen,
        List<Intervalo> candidatosSillon,
        List<Intervalo> candidatosProfesional,
        List<Intervalo> candidatosPaciente) {

    public ContextoDeTurno {
        Objects.requireNonNull(ahora, "ahora");
        Objects.requireNonNull(turno, "turno");
        Objects.requireNonNull(margen, "margen");
        candidatosSillon = List.copyOf(candidatosSillon);
        candidatosProfesional = List.copyOf(candidatosProfesional);
        candidatosPaciente = List.copyOf(candidatosPaciente);
    }

    /** Franja de atención de un día: {@code apertura < cierre} (lo garantiza la entidad de C-01). */
    public record Franja(LocalTime apertura, LocalTime cierre) {
    }
}
