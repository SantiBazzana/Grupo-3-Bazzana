package ar.odonto.turnos.domain.model;

/** Estados de un turno. ASISTIO y AUSENTE llegan en etapas posteriores (US-112). */
public enum TurnoEstado {
    RESERVADO,
    CANCELADO
}
