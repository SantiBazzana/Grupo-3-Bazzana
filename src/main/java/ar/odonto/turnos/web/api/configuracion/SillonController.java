package ar.odonto.turnos.web.api.configuracion;

import ar.odonto.turnos.application.configuracion.AltaSillon;
import ar.odonto.turnos.application.configuracion.EditarSillon;
import ar.odonto.turnos.application.configuracion.ListarSillones;
import ar.odonto.turnos.application.configuracion.SillonVista;
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

/** API de sillones de C-03: solo traduce DTOs; las reglas viven en los casos de uso. */
@RestController
@RequestMapping("/api/sillones")
class SillonController {

    private final AltaSillon altaSillon;
    private final EditarSillon editarSillon;
    private final ListarSillones listarSillones;

    SillonController(AltaSillon altaSillon, EditarSillon editarSillon, ListarSillones listarSillones) {
        this.altaSillon = altaSillon;
        this.editarSillon = editarSillon;
        this.listarSillones = listarSillones;
    }

    @GetMapping
    List<SillonResponse> listar() {
        return listarSillones.ejecutar().stream().map(SillonResponse::de).toList();
    }

    @PostMapping
    ResponseEntity<SillonResponse> crear(@Valid @RequestBody CrearSillonRequest request) {
        SillonVista creado = altaSillon.ejecutar(new AltaSillon.Comando(request.nombre()));
        return ResponseEntity.created(URI.create("/api/sillones/" + creado.id()))
                .body(SillonResponse.de(creado));
    }

    @PutMapping("/{id}")
    SillonResponse editar(@PathVariable Long id, @RequestBody EditarSillonRequest request) {
        return SillonResponse.de(editarSillon.ejecutar(
                new EditarSillon.Comando(id, request.nombre(), request.activo())));
    }
}
