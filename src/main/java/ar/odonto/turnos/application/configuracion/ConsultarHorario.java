package ar.odonto.turnos.application.configuracion;

import ar.odonto.turnos.domain.model.HorarioAtencion;
import ar.odonto.turnos.domain.port.HorarioAtencionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Devuelve los siete días de la semana; los días sin franja quedan cerrados (SU-03 / PA-02). */
@Service
public class ConsultarHorario {

    private final HorarioAtencionRepository horarios;

    public ConsultarHorario(HorarioAtencionRepository horarios) {
        this.horarios = horarios;
    }

    @Transactional(readOnly = true)
    public List<HorarioVista> ejecutar() {
        Map<DayOfWeek, HorarioAtencion> porDia = horarios.findAll().stream()
                .collect(Collectors.toMap(HorarioAtencion::getDiaSemana, Function.identity(),
                        (primera, segunda) -> primera));
        return Arrays.stream(DayOfWeek.values())
                .map(dia -> {
                    HorarioAtencion franja = porDia.get(dia);
                    return franja == null
                            ? new HorarioVista(dia, null, null)
                            : new HorarioVista(dia, franja.getApertura(), franja.getCierre());
                })
                .toList();
    }
}
