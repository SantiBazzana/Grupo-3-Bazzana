package ar.odonto.turnos.domain.model;

/**
 * Quién hace la solicitud. Lo usan los casos de uso para auditoría y reglas de visibilidad
 * (por ejemplo RN-AG-02 en C-07) sin depender de Spring Security. La implementación real la provee C-05
 * a través del puerto {@code UsuarioActual}.
 *
 * @param usuario       nombre de usuario (por ejemplo para {@code Turno.creadoPor})
 * @param rol           rol del usuario
 * @param profesionalId profesional vinculado, solo para ODONTOLOGO; null en otro caso
 */
public record Solicitante(String usuario, Rol rol, Long profesionalId) {

    /** Solicitante por defecto mientras rige la seguridad permisiva de C-01. */
    public static final Solicitante SISTEMA = new Solicitante("sistema", Rol.ADMIN, null);

    public boolean esOdontologo() {
        return rol == Rol.ODONTOLOGO;
    }
}
