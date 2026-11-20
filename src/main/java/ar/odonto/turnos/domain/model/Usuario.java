package ar.odonto.turnos.domain.model;

import ar.odonto.turnos.domain.error.CodigoError;
import ar.odonto.turnos.domain.error.ReglaDeNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/**
 * Usuario del consultorio. Solo guarda el hash de la contraseña (RN-AU-02).
 * Un ODONTOLOGO debe estar vinculado a un Profesional (RN-AU-04).
 */
@Entity
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profesional_id")
    private Profesional profesional;

    @Column(nullable = false)
    private boolean activo = true;

    protected Usuario() {
    }

    public Usuario(String username, String passwordHash, Rol rol, Profesional profesional) {
        if (rol == Rol.ODONTOLOGO && profesional == null) {
            throw new ReglaDeNegocioException(CodigoError.ENTRADA_INVALIDA,
                    "Un usuario ODONTOLOGO debe estar vinculado a un profesional");
        }
        this.username = username;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.profesional = profesional;
    }

    public void cambiarPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void desactivar() { this.activo = false; }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public Rol getRol() { return rol; }
    public Profesional getProfesional() { return profesional; }
    public boolean isActivo() { return activo; }

    @Override
    public String toString() {
        return "Usuario[id=" + id + ", username=" + username + ", rol=" + rol + "]";
    }
}
