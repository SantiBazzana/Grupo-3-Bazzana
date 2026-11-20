package ar.odonto.turnos.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/** Odontólogo del consultorio. Matrícula única (RN-CF-04); no se borra, se desactiva (RN-CF-03). */
@Entity
public class Profesional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Column(nullable = false, unique = true)
    private String matricula;

    private String telefono;

    @Column(nullable = false)
    private boolean activo = true;

    protected Profesional() {
    }

    public Profesional(String nombre, String apellido, String matricula, String telefono) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.matricula = matricula;
        this.telefono = telefono;
    }

    public void actualizar(String nombre, String apellido, String telefono) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.telefono = telefono;
    }

    public void desactivar() { this.activo = false; }
    public void activar() { this.activo = true; }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getMatricula() { return matricula; }
    public String getTelefono() { return telefono; }
    public boolean isActivo() { return activo; }
}
