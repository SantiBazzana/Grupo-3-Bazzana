package ar.odonto.turnos.application.configuracion;

import ar.odonto.turnos.application.configuracion.persistencia.RepositorioDeHorarioDeConfiguracion;
import ar.odonto.turnos.domain.error.CodigoError;
import ar.odonto.turnos.domain.error.ReglaDeNegocioException;
import ar.odonto.turnos.domain.model.Consultorio;
import ar.odonto.turnos.domain.model.HorarioAtencion;
import ar.odonto.turnos.domain.port.ConsultorioRepository;
import ar.odonto.turnos.domain.port.HorarioAtencionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;

/** Reemplaza o cierra la franja de un día (RN-CF-02, decisiones a y b). */
@Service
public class ReemplazarHorario {

    public record Comando(DayOfWeek diaSemana, LocalTime apertura, LocalTime cierre) {
    }

    private final ConsultorioRepository consultorios;
    private final HorarioAtencionRepository horarios;
    private final RepositorioDeHorarioDeConfiguracion horariosDeConfiguracion;

    public ReemplazarHorario(ConsultorioRepository consultorios, HorarioAtencionRepository horarios,
                             RepositorioDeHorarioDeConfiguracion horariosDeConfiguracion) {
        this.consultorios = consultorios;
        this.horarios = horarios;
        this.horariosDeConfiguracion = horariosDeConfiguracion;
    }

    @Transactional
    public HorarioVista ejecutar(Comando comando) {
        Consultorio consultorio = consultorios.findAll().stream().findFirst()
                .orElseThrow(() -> new ReglaDeNegocioException(CodigoError.RECURSO_INEXISTENTE,
                        "No existe el consultorio indicado"));
        DayOfWeek dia = comando.diaSemana();
        boolean cerrar = comando.apertura() == null && comando.cierre() == null;
        if (cerrar) {
            horariosDeConfiguracion.findByConsultorioIdAndDiaSemana(consultorio.getId(), dia)
                    .ifPresent(horariosDeConfiguracion::delete);
            return new HorarioVista(dia, null, null);
        }
        HorarioAtencion franja = horarios.findByConsultorioIdAndDiaSemana(consultorio.getId(), dia)
                .orElse(null);
        if (franja == null) {
            franja = horarios.save(new HorarioAtencion(consultorio, dia, comando.apertura(), comando.cierre()));
        } else {
            franja.cambiarFranja(comando.apertura(), comando.cierre());
            horarios.save(franja);
        }
        return new HorarioVista(dia, franja.getApertura(), franja.getCierre());
    }
}
