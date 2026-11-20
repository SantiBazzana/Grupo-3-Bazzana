package ar.odonto.turnos.domain.rules.turnos;

import ar.odonto.turnos.domain.error.CodigoError;
import ar.odonto.turnos.domain.error.ReglaDeNegocioException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Reglas de creación de turnos (knowledge-base/05, RN-TU), en Java puro (regla J1).
 * <p>
 * Lanza {@link ReglaDeNegocioException} con la PRIMERA regla violada, en el orden RN-TU-09:
 * recursos inactivos (08) → inicio en múltiplos de 5 (11) → pasado (06) → horario (07) → sillón (03)
 * → profesional (02, salvo sobreturno 04) → paciente (05).
 * La inexistencia de recursos y el sillón ausente se detectan antes, al cargar (design D2 de C-02).
 */
public final class ValidadorDeTurno {

    private ValidadorDeTurno() {
    }

    public static void validar(ContextoDeTurno c) {
        validarRecursosActivos(c);
        validarInicioEnMultiplosDe5(c.turno().inicio());
        validarNoEnPasado(c);
        validarDentroDelHorario(c);
        validarSillon(c);
        validarProfesional(c);
        validarPaciente(c);
    }

    // RN-TU-08
    private static void validarRecursosActivos(ContextoDeTurno c) {
        if (!c.sillonActivo()) {
            throw new ReglaDeNegocioException(CodigoError.RECURSO_INACTIVO, "El sillón está inactivo");
        }
        if (!c.profesionalActivo()) {
            throw new ReglaDeNegocioException(CodigoError.RECURSO_INACTIVO, "El profesional está inactivo");
        }
        if (!c.prestacionActiva()) {
            throw new ReglaDeNegocioException(CodigoError.RECURSO_INACTIVO, "La prestación está inactiva");
        }
    }

    // RN-TU-11
    private static void validarInicioEnMultiplosDe5(LocalDateTime inicio) {
        if (inicio.getMinute() % 5 != 0 || inicio.getSecond() != 0 || inicio.getNano() != 0) {
            throw new ReglaDeNegocioException(CodigoError.ENTRADA_INVALIDA,
                    "El inicio del turno debe estar en múltiplos de 5 minutos, sin segundos");
        }
    }

    // RN-TU-06
    private static void validarNoEnPasado(ContextoDeTurno c) {
        if (!c.turno().inicio().isAfter(c.ahora())) {
            throw new ReglaDeNegocioException(CodigoError.TURNO_EN_PASADO);
        }
    }

    // RN-TU-07: el turno completo dentro de la franja del día; sin franja = consultorio cerrado
    private static void validarDentroDelHorario(ContextoDeTurno c) {
        ContextoDeTurno.Franja franja = c.franja();
        Intervalo turno = c.turno();
        boolean mismoDia = turno.fin().toLocalDate().equals(turno.inicio().toLocalDate());
        if (franja == null || !mismoDia
                || turno.inicio().toLocalTime().isBefore(franja.apertura())
                || turno.fin().toLocalTime().isAfter(franja.cierre())) {
            throw new ReglaDeNegocioException(CodigoError.FUERA_DE_HORARIO);
        }
    }

    // RN-TU-03: también para sobreturnos
    private static void validarSillon(ContextoDeTurno c) {
        if (algunoChocaConMargen(c.turno(), c.candidatosSillon(), c)) {
            throw new ReglaDeNegocioException(CodigoError.CHOQUE_SILLON);
        }
    }

    // RN-TU-02 y RN-TU-04: el sobreturno se exime por completo, margen incluido (PA-03)
    private static void validarProfesional(ContextoDeTurno c) {
        if (!c.sobreturno() && algunoChocaConMargen(c.turno(), c.candidatosProfesional(), c)) {
            throw new ReglaDeNegocioException(CodigoError.CHOQUE_PROFESIONAL);
        }
    }

    // RN-TU-05: sin margen
    private static void validarPaciente(ContextoDeTurno c) {
        if (c.candidatosPaciente().stream().anyMatch(c.turno()::seSuperpone)) {
            throw new ReglaDeNegocioException(CodigoError.CHOQUE_PACIENTE);
        }
    }

    private static boolean algunoChocaConMargen(Intervalo turno, List<Intervalo> candidatos, ContextoDeTurno c) {
        return candidatos.stream().anyMatch(otro -> turno.chocaConMargen(otro, c.margen()));
    }
}
