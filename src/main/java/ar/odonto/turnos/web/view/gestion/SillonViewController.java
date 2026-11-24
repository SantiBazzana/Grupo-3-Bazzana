package ar.odonto.turnos.web.view.gestion;

import ar.odonto.turnos.application.configuracion.AltaSillon;
import ar.odonto.turnos.application.configuracion.ListarSillones;
import ar.odonto.turnos.domain.error.ReglaDeNegocioException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Pantalla de sillones de C-09: consume los casos de uso de C-03 sin duplicar reglas. */
@Controller
@RequestMapping("/gestion/sillones")
class SillonViewController {

    private static final String VISTA = "gestion/sillones";

    private final ListarSillones listarSillones;
    private final AltaSillon altaSillon;

    SillonViewController(ListarSillones listarSillones, AltaSillon altaSillon) {
        this.listarSillones = listarSillones;
        this.altaSillon = altaSillon;
    }

    @GetMapping
    String listar(Model model) {
        model.addAttribute("sillones", listarSillones.ejecutar());
        return VISTA;
    }

    @PostMapping
    String crear(@RequestParam String nombre, Model model) {
        try {
            altaSillon.ejecutar(new AltaSillon.Comando(nombre));
            return "redirect:/gestion/sillones";
        } catch (ReglaDeNegocioException ex) {
            model.addAttribute("errorCodigo", ex.codigo().name());
            model.addAttribute("errorMensaje", ex.getMessage());
            model.addAttribute("sillones", listarSillones.ejecutar());
            return VISTA;
        }
    }
}
