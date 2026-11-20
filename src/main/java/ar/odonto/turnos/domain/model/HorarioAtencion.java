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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.DayOfWeek;
import java.time.LocalTime;

/** Franja de atención del consultorio para un día de la semana (RN-CF-02). Días sin franja = cerrado. */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_horario_consultorio_dia",
        columnNames = {"consultorio_id", "dia_semana"}))
public class HorarioAtencion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "consultorio_id")
    private Consultorio consultorio;

    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana", nullable = false)
    private DayOfWeek diaSemana;

    @Column(nullable = false)
    private LocalTime apertura;

    @Column(nullable = false)
    private LocalTime cierre;

    protected HorarioAtencion() {
    }

    public HorarioAtencion(Consultorio consultorio, DayOfWeek diaSemana, LocalTime apertura, LocalTime cierre) {
        validarFranja(apertura, cierre);
        this.consultorio = consultorio;
        this.diaSemana = diaSemana;
        this.apertura = apertura;
        this.cierre = cierre;
    }

    public void cambiarFranja(LocalTime apertura, LocalTime cierre) {
        validarFranja(apertura, cierre);
        this.apertura = apertura;
        this.cierre = cierre;
    }

    private static void validarFranja(LocalTime apertura, LocalTime cierre) {
        if (apertura == null || cierre == null || !apertura.isBefore(cierre)) {
            throw new ReglaDeNegocioException(CodigoError.HORARIO_INVALIDO,
                    "La apertura (" + apertura + ") debe ser anterior al cierre (" + cierre + ")");
        }
    }

    public Long getId() { return id; }
    public Consultorio getConsultorio() { return consultorio; }
    public DayOfWeek getDiaSemana() { return diaSemana; }
    public LocalTime getApertura() { return apertura; }
    public LocalTime getCierre() { return cierre; }
}
