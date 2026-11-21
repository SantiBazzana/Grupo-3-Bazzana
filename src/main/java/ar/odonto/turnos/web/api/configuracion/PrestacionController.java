package ar.odonto.turnos.web.api.configuracion;

import ar.odonto.turnos.application.configuracion.AltaPrestacion;
import ar.odonto.turnos.application.configuracion.EditarPrestacion;
import ar.odonto.turnos.application.configuracion.ListarPrestaciones;
import ar.odonto.turnos.application.configuracion.PrestacionVista;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/** API de prestaciones de C-03: solo traduce DTOs; las reglas viven en los casos de uso. */
@RestController
@RequestMapping("/api/prestaciones")
class PrestacionController {

    private final AltaPrestacion altaPrestacion;
    private final EditarPrestacion editarPrestacion;
    private final ListarPrestaciones listarPrestaciones;

    PrestacionController(AltaPrestacion altaPrestacion, EditarPrestacion editarPrestacion,
                         ListarPrestaciones listarPrestaciones) {
        this.altaPrestacion = altaPrestacion;
        this.editarPrestacion = editarPrestacion;
        this.listarPrestaciones = listarPrestaciones;
    }

    @GetMapping
    List<PrestacionResponse> listar() {
        return listarPrestaciones.ejecutar().stream().map(PrestacionResponse::de).toList();
    }

    @PostMapping
    ResponseEntity<PrestacionResponse> crear(@Valid @RequestBody CrearPrestacionRequest request) {
        PrestacionVista creada = altaPrestacion.ejecutar(
                new AltaPrestacion.Comando(request.nombre(), request.duracionMinutos()));
        return ResponseEntity.created(URI.create("/api/prestaciones/" + creada.id()))
                .body(PrestacionResponse.de(creada));
    }

    @PutMapping("/{id}")
    PrestacionResponse editar(@PathVariable Long id, @Valid @RequestBody EditarPrestacionRequest request) {
        return PrestacionResponse.de(editarPrestacion.ejecutar(new EditarPrestacion.Comando(id, request.nombre(),
                request.duracionMinutos(), request.activa())));
    }
}
