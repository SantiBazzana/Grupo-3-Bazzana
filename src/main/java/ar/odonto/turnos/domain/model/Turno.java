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
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Reserva de un profesional + sillón + paciente + prestación en el intervalo {@code [inicio, fin)}.
 * <p>
 * La entidad solo protege sus invariantes de datos ({@code fin > inicio}, sillón presente, transición de estado).
 * Las REGLAS de negocio (cálculo del fin RN-TU-01, choques RN-TU-02..05, horario RN-TU-07, cancelación RN-CA)
 * viven en {@code domain.rules} y pertenecen a C-02 / C-08.
 */
@Entity
@Table(indexes = {
        @Index(name = "ix_turno_profesional_inicio", columnList = "profesional_id, inicio"),
        @Index(name = "ix_turno_sillon_inicio", columnList = "sillon_id, inicio"),
        @Index(name = "ix_turno_paciente_inicio", columnList = "paciente_id, inicio")
})
public class Turno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profesional_id")
    private Profesional profesional;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sillon_id")
    private Sillon sillon;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prestacion_id")
    private Prestacion prestacion;

    @Column(nullable = false)
    private LocalDateTime inicio;

    @Column(nullable = false)
    private LocalDateTime fin;

    @Column(nullable = false)
    private boolean sobreturno;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TurnoEstado estado = TurnoEstado.RESERVADO;

    private LocalDateTime canceladoEn;

    @Column(nullable = false)
    private LocalDateTime creadoEn;

    @Column(nullable = false)
    private String creadoPor;

    protected Turno() {
    }

    public Turno(Profesional profesional, Sillon sillon, Paciente paciente, Prestacion prestacion,
            LocalDateTime inicio, LocalDateTime fin, boolean sobreturno, LocalDateTime creadoEn, String creadoPor) {
        if (sillon == null) {
            throw new ReglaDeNegocioException(CodigoError.SILLON_REQUERIDO);
        }
        if (inicio == null || fin == null || !fin.isAfter(inicio)) {
            throw new ReglaDeNegocioException(CodigoError.ENTRADA_INVALIDA,
                    "El fin del turno debe ser posterior al inicio");
        }
        this.profesional = profesional;
        this.sillon = sillon;
        this.paciente = paciente;
        this.prestacion = prestacion;
        this.inicio = inicio;
        this.fin = fin;
        this.sobreturno = sobreturno;
        this.creadoEn = creadoEn;
        this.creadoPor = creadoPor;
    }

    /** Transición a CANCELADO. Las condiciones de negocio (anticipación mínima, RN-CA-01) las valida C-08. */
    public void marcarCancelado(LocalDateTime cuando) {
        if (estado == TurnoEstado.CANCELADO) {
            throw new ReglaDeNegocioException(CodigoError.TURNO_YA_CANCELADO);
        }
        this.estado = TurnoEstado.CANCELADO;
        this.canceladoEn = cuando;
    }

    public boolean estaActivo() { return estado == TurnoEstado.RESERVADO; }

    public Long getId() { return id; }
    public Profesional getProfesional() { return profesional; }
    public Sillon getSillon() { return sillon; }
    public Paciente getPaciente() { return paciente; }
    public Prestacion getPrestacion() { return prestacion; }
    public LocalDateTime getInicio() { return inicio; }
    public LocalDateTime getFin() { return fin; }
    public boolean isSobreturno() { return sobreturno; }
    public TurnoEstado getEstado() { return estado; }
    public LocalDateTime getCanceladoEn() { return canceladoEn; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public String getCreadoPor() { return creadoPor; }

    @Override
    public String toString() {
        return "Turno[id=" + id + ", inicio=" + inicio + ", fin=" + fin + ", estado=" + estado + "]";
    }
}
