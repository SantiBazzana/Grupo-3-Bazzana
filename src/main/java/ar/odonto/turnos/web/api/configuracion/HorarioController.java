package ar.odonto.turnos.web.api.configuracion;

import ar.odonto.turnos.application.configuracion.ConsultarHorario;
import ar.odonto.turnos.application.configuracion.HorarioVista;
import ar.odonto.turnos.application.configuracion.ReemplazarHorario;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** API de lectura del horario del consultorio (C-03): sin lógica de negocio (J1). */
@RestController
@RequestMapping("/api/consultorio/horario")
class HorarioController {

    private final ConsultarHorario consultarHorario;
    private final ReemplazarHorario reemplazarHorario;

    HorarioController(ConsultarHorario consultarHorario, ReemplazarHorario reemplazarHorario) {
        this.consultarHorario = consultarHorario;
        this.reemplazarHorario = reemplazarHorario;
    }

    @GetMapping
    List<HorarioDiaResponse> consultar() {
        return consultarHorario.ejecutar().stream().map(HorarioDiaResponse::de).toList();
    }

    @PutMapping
    HorarioDiaResponse reemplazar(@Valid @RequestBody ReemplazarHorarioRequest request) {
        HorarioVista vista = reemplazarHorario.ejecutar(new ReemplazarHorario.Comando(
                request.dia(), request.apertura(), request.cierre()));
        return HorarioDiaResponse.de(vista);
    }
}
