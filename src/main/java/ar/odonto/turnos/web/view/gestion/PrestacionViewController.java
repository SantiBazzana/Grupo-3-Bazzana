package ar.odonto.turnos.web.view.gestion;

import ar.odonto.turnos.application.configuracion.AltaPrestacion;
import ar.odonto.turnos.application.configuracion.ListarPrestaciones;
import ar.odonto.turnos.domain.error.ReglaDeNegocioException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Pantalla de prestaciones de C-09: la duración válida la valida el modelo (RN-CF-01). */
@Controller
@RequestMapping("/gestion/prestaciones")
class PrestacionViewController {

    private static final String VISTA = "gestion/prestaciones";

    private final ListarPrestaciones listarPrestaciones;
    private final AltaPrestacion altaPrestacion;

    PrestacionViewController(ListarPrestaciones listarPrestaciones, AltaPrestacion altaPrestacion) {
        this.listarPrestaciones = listarPrestaciones;
        this.altaPrestacion = altaPrestacion;
    }

    @GetMapping
    String listar(Model model) {
        model.addAttribute("prestaciones", listarPrestaciones.ejecutar());
        return VISTA;
    }

    @PostMapping
    String crear(@RequestParam String nombre, @RequestParam int duracionMinutos, Model model) {
        try {
            altaPrestacion.ejecutar(new AltaPrestacion.Comando(nombre, duracionMinutos));
            return "redirect:/gestion/prestaciones";
        } catch (ReglaDeNegocioException ex) {
            model.addAttribute("errorCodigo", ex.codigo().name());
            model.addAttribute("errorMensaje", ex.getMessage());
            model.addAttribute("prestaciones", listarPrestaciones.ejecutar());
            return VISTA;
        }
    }
}
