package ar.odonto.turnos.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/** Sillón (box) del consultorio. Nombre único (RN-CF-04); no se borra, se desactiva (RN-CF-03). */
@Entity
public class Sillon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(nullable = false)
    private boolean activo = true;

    protected Sillon() {
    }

    public Sillon(String nombre) {
        this.nombre = nombre;
    }

    public void renombrar(String nombre) { this.nombre = nombre; }
    public void desactivar() { this.activo = false; }
    public void activar() { this.activo = true; }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public boolean isActivo() { return activo; }
}
