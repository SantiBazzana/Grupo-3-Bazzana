package ar.odonto.turnos.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/** Consultorio. La v1 tiene uno solo; la entidad existe para no rehacer el modelo con multisede. */
@Entity
public class Consultorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String zonaHoraria;

    protected Consultorio() {
    }

    public Consultorio(String nombre, String zonaHoraria) {
        this.nombre = nombre;
        this.zonaHoraria = zonaHoraria;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getZonaHoraria() { return zonaHoraria; }
}
