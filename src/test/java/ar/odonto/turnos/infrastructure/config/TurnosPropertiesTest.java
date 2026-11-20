package ar.odonto.turnos.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Clock;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Spec base-del-sistema: "Parámetros de negocio configurables" y "Zona horaria y reloj del sistema".
 */
class TurnosPropertiesTest {

    private final ApplicationContextRunner contexto = new ApplicationContextRunner()
            .withUserConfiguration(Configuracion.class, RelojConfig.class);

    @EnableConfigurationProperties(TurnosProperties.class)
    static class Configuracion {
    }

    @Test
    void valoresPorDefecto_margen5MinutosYCancelacion6Horas() {
        contexto.run(ctx -> {
            TurnosProperties propiedades = ctx.getBean(TurnosProperties.class);
            assertThat(propiedades.margen()).isEqualTo(Duration.ofMinutes(5));
            assertThat(propiedades.anticipacionMinimaCancelacion()).isEqualTo(Duration.ofHours(6));
            assertThat(propiedades.zona()).isEqualTo(ZoneId.of("America/Argentina/Buenos_Aires"));
        });
    }

    @Test
    void valorConfigurado_margenDe10Minutos() {
        contexto.withPropertyValues("turnos.margen-minutos=10")
                .run(ctx -> assertThat(ctx.getBean(TurnosProperties.class).margen())
                        .isEqualTo(Duration.ofMinutes(10)));
    }

    @Test
    void servidorEnUtc_elRelojDeNegocioUsaLaZonaArgentina() {
        TimeZone original = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
            contexto.run(ctx -> {
                Clock reloj = ctx.getBean(Clock.class);
                ZonedDateTime ahora = ZonedDateTime.now(reloj);
                assertThat(reloj.getZone()).isEqualTo(ZoneId.of("America/Argentina/Buenos_Aires"));
                assertThat(ahora.getOffset()).isEqualTo(ZoneOffset.ofHours(-3));
            });
        } finally {
            TimeZone.setDefault(original);
        }
    }
}
