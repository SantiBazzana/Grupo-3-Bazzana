package ar.odonto.turnos.web.api.configuracion;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Cuerpo de {@code PUT /api/consultorio/horario}: franja completa o día cerrado (ambos null).
 * El día se valida como texto para que los errores de formato nombren el campo inválido (spec 8.1).
 */
public record ReemplazarHorarioRequest(
        @NotNull
        @Pattern(regexp = "MONDAY|TUESDAY|WEDNESDAY|THURSDAY|FRIDAY|SATURDAY|SUNDAY",
                message = "debe ser un día de la semana válido")
        String diaSemana,
        LocalTime apertura,
        LocalTime cierre) {

    @AssertTrue(message = "apertura y cierre deben enviarse juntos o ambos null")
    public boolean isFranjaCompletaOVacia() {
        return (apertura == null) == (cierre == null);
    }

    DayOfWeek dia() {
        return DayOfWeek.valueOf(diaSemana);
    }
}
