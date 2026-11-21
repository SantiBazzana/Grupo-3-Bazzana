package ar.odonto.turnos.application.configuracion;

import java.time.DayOfWeek;
import java.time.LocalTime;

/** Franja de un día de la semana; {@code null} en ambos horarios significa día cerrado (decisión a). */
public record HorarioVista(DayOfWeek diaSemana, LocalTime apertura, LocalTime cierre) {
}
