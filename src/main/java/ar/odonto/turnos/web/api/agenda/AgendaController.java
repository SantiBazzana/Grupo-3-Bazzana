package ar.odonto.turnos.web.api.agenda;

import ar.odonto.turnos.application.agenda.ConsultarAgenda;
import ar.odonto.turnos.application.agenda.TurnoDeAgendaVista;
import ar.odonto.turnos.domain.port.UsuarioActual;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * API de agenda del día (C-07, US-020). Sin lógica de negocio (regla J1): obtiene el solicitante de
 * UsuarioActual y delega en el caso de uso, que aplica RN-AG-02. Los errores los traduce el manejador
 * global de C-01 a Problem Details con {@code codigo}.
 */
@RestController
@RequestMapping("/api/agenda")
class AgendaController {

    private final ConsultarAgenda consultarAgenda;
    private final UsuarioActual usuarioActual;

    AgendaController(ConsultarAgenda consultarAgenda, UsuarioActual usuarioActual) {
        this.consultarAgenda = consultarAgenda;
        this.usuarioActual = usuarioActual;
    }

    @GetMapping
    AgendaDelDiaResponse agenda(@RequestParam Long profesionalId, @RequestParam LocalDate fecha) {
        List<TurnoDeAgendaVista> turnos = consultarAgenda.ejecutar(profesionalId, fecha, usuarioActual.solicitante());
        return AgendaDelDiaResponse.de(fecha, profesionalId, turnos);
    }
}
