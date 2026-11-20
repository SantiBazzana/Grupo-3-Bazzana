package ar.odonto.turnos.web.api.turnos;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Cuerpo de {@code POST /api/turnos} (design D5 de C-02).
 * {@code sillonId} NO es obligatorio a nivel formato: su ausencia es la regla de negocio {@code SILLON_REQUERIDO} (422).
 * No tiene campo {@code fin}: si el cliente lo envía se ignora (RN-TU-01).
 */
public record CrearTurnoRequest(
        @NotNull Long pacienteId,
        @NotNull Long profesionalId,
        Long sillonId,
        @NotNull Long prestacionId,
        @NotNull LocalDateTime inicio,
        Boolean sobreturno) {

    public boolean esSobreturno() {
        return Boolean.TRUE.equals(sobreturno);
    }
}
