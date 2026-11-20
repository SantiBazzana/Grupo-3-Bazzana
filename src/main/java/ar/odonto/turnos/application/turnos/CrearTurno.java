package ar.odonto.turnos.application.turnos;

import ar.odonto.turnos.domain.error.CodigoError;
import ar.odonto.turnos.domain.error.ReglaDeNegocioException;
import ar.odonto.turnos.domain.model.Paciente;
import ar.odonto.turnos.domain.model.Prestacion;
import ar.odonto.turnos.domain.model.Profesional;
import ar.odonto.turnos.domain.model.Sillon;
import ar.odonto.turnos.domain.model.Turno;
import ar.odonto.turnos.domain.port.ConsultorioRepository;
import ar.odonto.turnos.domain.port.HorarioAtencionRepository;
import ar.odonto.turnos.domain.port.PrestacionRepository;
import ar.odonto.turnos.domain.port.TurnoRepository;
import ar.odonto.turnos.domain.port.UsuarioActual;
import ar.odonto.turnos.domain.rules.turnos.ContextoDeTurno;
import ar.odonto.turnos.domain.rules.turnos.Intervalo;
import ar.odonto.turnos.domain.rules.turnos.ValidadorDeTurno;
import ar.odonto.turnos.infrastructure.config.TurnosProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Caso de uso "crear turno" (US-010, design D3 de C-02). Todo ocurre en UNA transacción (RN-TU-10):
 * bloqueo de recursos → cálculo del fin → horario → candidatos de choque → validación → guardado.
 */
@Service
public class CrearTurno {

    /** Datos de entrada. El fin no se recibe: se calcula con la duración de la prestación (RN-TU-01). */
    public record Comando(Long pacienteId, Long profesionalId, Long sillonId, Long prestacionId,
            LocalDateTime inicio, boolean sobreturno) {
    }

    private final BloqueoDeRecursos bloqueo;
    private final PrestacionRepository prestaciones;
    private final ConsultorioRepository consultorios;
    private final HorarioAtencionRepository horarios;
    private final TurnoRepository turnos;
    private final UsuarioActual usuarioActual;
    private final TurnosProperties propiedades;
    private final Clock reloj;

    public CrearTurno(BloqueoDeRecursos bloqueo, PrestacionRepository prestaciones, ConsultorioRepository consultorios,
            HorarioAtencionRepository horarios, TurnoRepository turnos, UsuarioActual usuarioActual,
            TurnosProperties propiedades, Clock reloj) {
        this.bloqueo = bloqueo;
        this.prestaciones = prestaciones;
        this.consultorios = consultorios;
        this.horarios = horarios;
        this.turnos = turnos;
        this.usuarioActual = usuarioActual;
        this.propiedades = propiedades;
        this.reloj = reloj;
    }

    @Transactional
    public TurnoVista ejecutar(Comando comando) {
        // RN-TU-03 / RN-TU-08: sillón obligatorio; bloqueo en orden fijo sillón → profesional → paciente (D4)
        if (comando.sillonId() == null) {
            throw new ReglaDeNegocioException(CodigoError.SILLON_REQUERIDO);
        }
        Sillon sillon = bloqueo.bloquearSillon(comando.sillonId()).orElseThrow(() -> inexistente("sillón"));
        Profesional profesional = bloqueo.bloquearProfesional(comando.profesionalId())
                .orElseThrow(() -> inexistente("profesional"));
        Paciente paciente = bloqueo.bloquearPaciente(comando.pacienteId()).orElseThrow(() -> inexistente("paciente"));
        Prestacion prestacion = prestaciones.findById(comando.prestacionId())
                .orElseThrow(() -> inexistente("prestación"));

        // RN-TU-01
        LocalDateTime inicio = comando.inicio();
        LocalDateTime fin = inicio.plus(prestacion.duracion());
        Intervalo intervalo = new Intervalo(inicio, fin);
        Duration margen = propiedades.margen();

        ContextoDeTurno contexto = new ContextoDeTurno(
                LocalDateTime.now(reloj), intervalo, comando.sobreturno(), franjaDelDia(inicio),
                sillon.isActivo(), profesional.isActivo(), prestacion.isActiva(), margen,
                intervalos(turnos.buscarChoquesDeSillon(sillon.getId(), inicio.minus(margen), fin.plus(margen), null)),
                intervalos(turnos.buscarChoquesDeProfesional(profesional.getId(), inicio.minus(margen),
                        fin.plus(margen), null)),
                intervalos(turnos.buscarSuperpuestosDePaciente(paciente.getId(), inicio, fin, null)));

        ValidadorDeTurno.validar(contexto);

        Turno turno = turnos.save(new Turno(profesional, sillon, paciente, prestacion, inicio, fin,
                comando.sobreturno(), contexto.ahora(), usuarioActual.solicitante().usuario()));
        return TurnoVista.de(turno);
    }

    /** Franja del único consultorio de la v1 (design D8); null si no hay consultorio o el día no tiene atención. */
    private ContextoDeTurno.Franja franjaDelDia(LocalDateTime inicio) {
        return consultorios.findAll().stream().findFirst()
                .flatMap(c -> horarios.findByConsultorioIdAndDiaSemana(c.getId(), inicio.getDayOfWeek()))
                .map(h -> new ContextoDeTurno.Franja(h.getApertura(), h.getCierre()))
                .orElse(null);
    }

    private static List<Intervalo> intervalos(List<Turno> candidatos) {
        return candidatos.stream().map(t -> new Intervalo(t.getInicio(), t.getFin())).toList();
    }

    private static ReglaDeNegocioException inexistente(String recurso) {
        return new ReglaDeNegocioException(CodigoError.RECURSO_INEXISTENTE, "No existe el " + recurso + " indicado");
    }
}
