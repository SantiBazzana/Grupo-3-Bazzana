package ar.odonto.turnos.web.api.error;

import ar.odonto.turnos.domain.error.CodigoError;
import ar.odonto.turnos.domain.error.ReglaDeNegocioException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador SOLO de test (vive en src/test): permite verificar el manejador global de errores
 * sin depender de los endpoints de negocio de otros changes.
 */
@RestController
@RequestMapping("/test/errores")
class ControladorDeErroresDePrueba {

    record SolicitudDePrueba(@NotBlank String nombre) {
    }

    @GetMapping("/regla/{codigo}")
    String regla(@PathVariable CodigoError codigo) {
        throw new ReglaDeNegocioException(codigo, "detalle de prueba para " + codigo.name());
    }

    @PostMapping("/validacion")
    String validacion(@Valid @RequestBody SolicitudDePrueba solicitud) {
        return "ok";
    }

    @GetMapping("/inesperado")
    String inesperado() {
        throw new IllegalStateException("falla interna con datos que no deben salir: DNI 00000001");
    }
}
