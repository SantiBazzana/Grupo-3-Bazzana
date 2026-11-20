package ar.odonto.turnos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Punto de entrada de la agenda de turnos odontológicos.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class TurnosApplication {

    public static void main(String[] args) {
        SpringApplication.run(TurnosApplication.class, args);
    }
}
