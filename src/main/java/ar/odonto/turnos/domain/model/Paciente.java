package ar.odonto.turnos.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * Paciente. Datos personales bajo Ley 25.326 (RN-DP-02): SIEMPRE ficticios en repo, seed y tests (RN-DP-01, regla D1),
 * y nunca en logs (RN-DP-03). Por eso {@code toString()} solo expone el id.
 */
@Entity
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Column(nullable = false, unique = true)
    private String dni;

    /** Formato internacional sin "+", por ejemplo 5491100000001 (RN-WA-01). */
    private String telefono;

    private String email;

    protected Paciente() {
    }

    public Paciente(String nombre, String apellido, String dni, String telefono, String email) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.telefono = telefono;
        this.email = email;
    }

    public void actualizar(String nombre, String apellido, String telefono, String email) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.telefono = telefono;
        this.email = email;
    }

    public boolean tieneTelefono() { return telefono != null && !telefono.isBlank(); }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getDni() { return dni; }
    public String getTelefono() { return telefono; }
    public String getEmail() { return email; }

    @Override
    public String toString() {
        return "Paciente[id=" + id + "]";
    }
}
