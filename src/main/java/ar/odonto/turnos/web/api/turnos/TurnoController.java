package ar.odonto.turnos.web.api.turnos;

import ar.odonto.turnos.application.turnos.ConsultarTurno;
import ar.odonto.turnos.application.turnos.CrearTurno;
import ar.odonto.turnos.application.turnos.TurnoVista;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * API de turnos (C-02). Sin lógica de negocio (regla J1): traduce DTOs y delega en los casos de uso.
 * Los errores los traduce el manejador global de C-01 a Problem Details con {@code codigo}.
 */
@RestController
@RequestMapping("/api/turnos")
class TurnoController {

    private final CrearTurno crearTurno;
    private final ConsultarTurno consultarTurno;

    TurnoController(CrearTurno crearTurno, ConsultarTurno consultarTurno) {
        this.crearTurno = crearTurno;
        this.consultarTurno = consultarTurno;
    }

    @PostMapping
    ResponseEntity<TurnoResponse> crear(@Valid @RequestBody CrearTurnoRequest request) {
        TurnoVista creado = crearTurno.ejecutar(new CrearTurno.Comando(request.pacienteId(), request.profesionalId(),
                request.sillonId(), request.prestacionId(), request.inicio(), request.esSobreturno()));
        return ResponseEntity.created(URI.create("/api/turnos/" + creado.id())).body(TurnoResponse.de(creado));
    }

    @GetMapping("/{id}")
    TurnoResponse detalle(@PathVariable Long id) {
        return TurnoResponse.de(consultarTurno.ejecutar(id));
    }
}
