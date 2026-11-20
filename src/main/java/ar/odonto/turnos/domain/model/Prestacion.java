package ar.odonto.turnos.domain.model;

import ar.odonto.turnos.domain.error.CodigoError;
import ar.odonto.turnos.domain.error.ReglaDeNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.Duration;

/** Prestación con duración fija (RN-TU-01). Duración > 0 y múltiplo de 5 (RN-CF-01); nombre único (RN-CF-04). */
@Entity
public class Prestacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(nullable = false)
    private int duracionMinutos;

    @Column(nullable = false)
    private boolean activa = true;

    protected Prestacion() {
    }

    public Prestacion(String nombre, int duracionMinutos) {
        validarDuracion(duracionMinutos);
        this.nombre = nombre;
        this.duracionMinutos = duracionMinutos;
    }

    public void actualizar(String nombre, int duracionMinutos) {
        validarDuracion(duracionMinutos);
        this.nombre = nombre;
        this.duracionMinutos = duracionMinutos;
    }

    private static void validarDuracion(int minutos) {
        if (minutos <= 0 || minutos % 5 != 0) {
            throw new ReglaDeNegocioException(CodigoError.DURACION_INVALIDA,
                    "Duración inválida: " + minutos + " minutos (debe ser mayor que 0 y múltiplo de 5)");
        }
    }

    public Duration duracion() { return Duration.ofMinutes(duracionMinutos); }
    public void desactivar() { this.activa = false; }
    public void activar() { this.activa = true; }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public int getDuracionMinutos() { return duracionMinutos; }
    public boolean isActiva() { return activa; }
}
