package ar.odonto.turnos.support;

import ar.odonto.turnos.domain.model.Consultorio;
import ar.odonto.turnos.domain.model.HorarioAtencion;
import ar.odonto.turnos.domain.model.Paciente;
import ar.odonto.turnos.domain.model.Prestacion;
import ar.odonto.turnos.domain.model.Profesional;
import ar.odonto.turnos.domain.model.Sillon;
import ar.odonto.turnos.domain.model.Turno;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Datos de prueba 100 % FICTICIOS (regla D1 / RN-DP-01): DNI 0000000X, teléfono 549110000000X, nombres de ejemplo.
 * Los objetos NO están persistidos: cada test los guarda con sus repositorios.
 */
public final class DatosDePrueba {

    private DatosDePrueba() {
    }

    public static Consultorio consultorio() {
        return new Consultorio("Consultorio Demo", "America/Argentina/Buenos_Aires");
    }

    public static HorarioAtencion horario(Consultorio consultorio, DayOfWeek dia) {
        return new HorarioAtencion(consultorio, dia, LocalTime.of(8, 0), LocalTime.of(20, 0));
    }

    public static Profesional profesional(String matricula) {
        return new Profesional("Ana", "Ejemplo", matricula, "5491100000100");
    }

    public static Sillon sillon(String nombre) {
        return new Sillon(nombre);
    }

    public static Prestacion prestacion(String nombre, int minutos) {
        return new Prestacion(nombre, minutos);
    }

    /** Paciente ficticio número {@code n} (1..9): DNI 0000000n, teléfono 549110000000n. */
    public static Paciente paciente(int n) {
        return new Paciente("Paciente", "Numero " + n, "0000000" + n, "549110000000" + n, null);
    }

    public static Turno turno(Profesional profesional, Sillon sillon, Paciente paciente, Prestacion prestacion,
            String inicio, String fin) {
        return new Turno(profesional, sillon, paciente, prestacion, LocalDateTime.parse(inicio),
                LocalDateTime.parse(fin), false, LocalDateTime.parse("2026-11-20T10:00"), "prueba");
    }
}
