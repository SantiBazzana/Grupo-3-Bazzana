package ar.odonto.turnos.web.api.configuracion;

import ar.odonto.turnos.application.configuracion.AltaProfesional;
import ar.odonto.turnos.application.configuracion.EditarProfesional;
import ar.odonto.turnos.application.configuracion.ListarProfesionales;
import ar.odonto.turnos.application.configuracion.ProfesionalVista;
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

/** API de profesionales de C-03: solo traduce DTOs; las reglas viven en los casos de uso. */
@RestController
@RequestMapping("/api/profesionales")
class ProfesionalController {

    private final AltaProfesional altaProfesional;
    private final EditarProfesional editarProfesional;
    private final ListarProfesionales listarProfesionales;

    ProfesionalController(AltaProfesional altaProfesional, EditarProfesional editarProfesional,
                          ListarProfesionales listarProfesionales) {
        this.altaProfesional = altaProfesional;
        this.editarProfesional = editarProfesional;
        this.listarProfesionales = listarProfesionales;
    }

    @GetMapping
    List<ProfesionalResponse> listar() {
        return listarProfesionales.ejecutar().stream().map(ProfesionalResponse::de).toList();
    }

    @PostMapping
    ResponseEntity<ProfesionalResponse> crear(@Valid @RequestBody CrearProfesionalRequest request) {
        ProfesionalVista creado = altaProfesional.ejecutar(new AltaProfesional.Comando(request.nombre(),
                request.apellido(), request.matricula(), request.telefono()));
        return ResponseEntity.created(URI.create("/api/profesionales/" + creado.id()))
                .body(ProfesionalResponse.de(creado));
    }

    @PutMapping("/{id}")
    ProfesionalResponse editar(@PathVariable Long id, @Valid @RequestBody EditarProfesionalRequest request) {
        return ProfesionalResponse.de(editarProfesional.ejecutar(new EditarProfesional.Comando(id, request.nombre(),
                request.apellido(), request.matricula(), request.telefono(), request.activo())));
    }
}
