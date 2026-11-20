package ar.odonto.turnos.infrastructure.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.time.ZoneId;

/**
 * Parámetros de negocio configurables (prefijo {@code turnos}).
 *
 * @param margenMinutos           margen mínimo entre turnos del mismo profesional o sillón (RN-TU-02/03)
 * @param cancelacionHorasMinimas anticipación mínima para cancelar un turno (RN-CA-01)
 * @param zonaHoraria             zona horaria del consultorio (RN-GL-01)
 */
@Validated
@ConfigurationProperties("turnos")
public record TurnosProperties(
        @DefaultValue("5") @Min(0) int margenMinutos,
        @DefaultValue("6") @Min(0) int cancelacionHorasMinimas,
        @DefaultValue("America/Argentina/Buenos_Aires") @NotBlank String zonaHoraria) {

    public Duration margen() {
        return Duration.ofMinutes(margenMinutos);
    }

    public Duration anticipacionMinimaCancelacion() {
        return Duration.ofHours(cancelacionHorasMinimas);
    }

    public ZoneId zona() {
        return ZoneId.of(zonaHoraria);
    }
}
