package ar.odonto.turnos.web.view.turnos;

/**
 * Datos de presentación del detalle de un turno (J2: nunca la entidad JPA). No incluye DNI ni teléfono
 * del paciente (reglas D1/D2); el enlace de WhatsApp se agrega por separado y solo para roles de gestión.
 *
 * @param inicio       fecha/hora de inicio ya formateada para la vista
 * @param fin          fecha/hora de fin ya formateada para la vista
 * @param whatsappUrl  enlace click-to-chat, o {@code null} si no corresponde mostrarlo
 * @param sinTelefono  {@code true} cuando el paciente no tiene teléfono (estado de UI, no error de página)
 */
public record DetalleDeTurno(Long id, String pacienteNombre, String profesionalNombre, String sillonNombre,
        String prestacionNombre, String inicio, String fin, String estado, boolean sobreturno, String creadoPor,
        String whatsappUrl, boolean sinTelefono) {
}
