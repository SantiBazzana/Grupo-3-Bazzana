package ar.odonto.turnos.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Contraseñas de los usuarios de ejemplo del perfil dev (design D9 de C-01).
 * Se leen de SEED_ADMIN_PASSWORD, SEED_SECRETARIA_PASSWORD y SEED_ODONTOLOGO_PASSWORD (ver application-dev.yml);
 * nunca se versionan (regla E3) ni se loguean (RN-AU-02).
 */
@ConfigurationProperties("turnos.seed")
public record SeedProperties(String adminPassword, String secretariaPassword, String odontologoPassword) {

    @Override
    public String toString() {
        return "SeedProperties[contraseñas ocultas]";
    }
}
