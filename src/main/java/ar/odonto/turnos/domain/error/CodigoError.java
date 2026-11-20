package ar.odonto.turnos.domain.error;

/**
 * Catálogo único de códigos de error de la API (knowledge-base/05_reglas_de_negocio.md).
 * <p>
 * Cada código tiene un estado HTTP fijo. Es Java puro (sin Spring) para que las reglas del dominio lo usen.
 * Agregar un código nuevo es un cambio compartido: solo vía el líder (anti-colisión 5 de CHANGES.md).
 */
public enum CodigoError {

    // Choques y duplicados (409)
    CHOQUE_PROFESIONAL(409, "El profesional ya tiene un turno en ese horario"),
    CHOQUE_SILLON(409, "El sillón ya está ocupado en ese horario"),
    CHOQUE_PACIENTE(409, "El paciente ya tiene un turno superpuesto"),
    DATO_DUPLICADO(409, "Ya existe un registro con ese dato único"),

    // Recursos (404)
    RECURSO_INEXISTENTE(404, "El recurso solicitado no existe"),

    // Entrada mal formada (400)
    ENTRADA_INVALIDA(400, "La solicitud tiene datos inválidos o mal formados"),

    // Acceso (401 / 403)
    NO_AUTENTICADO(401, "Se requiere iniciar sesión"),
    ACCESO_DENEGADO(403, "No tiene permiso para esta operación"),

    // Reglas de negocio (422)
    SILLON_REQUERIDO(422, "El turno debe ocupar un sillón"),
    TURNO_EN_PASADO(422, "No se puede crear un turno en el pasado"),
    FUERA_DE_HORARIO(422, "El turno está fuera del horario de atención"),
    RECURSO_INACTIVO(422, "El recurso está inactivo"),
    CANCELACION_TARDIA(422, "No se puede cancelar con menos anticipación que la mínima"),
    TURNO_YA_CANCELADO(422, "El turno ya estaba cancelado"),
    PACIENTE_SIN_TELEFONO(422, "El paciente no tiene teléfono cargado"),
    DURACION_INVALIDA(422, "La duración debe ser mayor que 0 y múltiplo de 5 minutos"),
    HORARIO_INVALIDO(422, "La apertura debe ser anterior al cierre"),

    // Inesperado (500)
    ERROR_INTERNO(500, "Error interno del servidor");

    private final int estadoHttp;
    private final String titulo;

    CodigoError(int estadoHttp, String titulo) {
        this.estadoHttp = estadoHttp;
        this.titulo = titulo;
    }

    public int estadoHttp() {
        return estadoHttp;
    }

    public String titulo() {
        return titulo;
    }
}
