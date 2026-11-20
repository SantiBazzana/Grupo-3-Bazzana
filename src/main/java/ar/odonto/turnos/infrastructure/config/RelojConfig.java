package ar.odonto.turnos.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Único reloj del sistema (RN-GL-02): ninguna regla usa {@code LocalDateTime.now()} sin este {@link Clock}.
 * En los tests se reemplaza por un reloj fijo (ver {@code support.RelojDePrueba}).
 */
@Configuration(proxyBeanMethods = false)
public class RelojConfig {

    @Bean
    Clock reloj(TurnosProperties propiedades) {
        return Clock.system(propiedades.zona());
    }
}
