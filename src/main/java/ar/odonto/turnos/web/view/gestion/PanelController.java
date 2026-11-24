package ar.odonto.turnos.web.view.gestion;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Panel de gestión: punto de llegada del login y puerta a las pantallas de C-09. */
@Controller
class PanelController {

    @GetMapping("/")
    String raiz() {
        return "redirect:/panel";
    }

    @GetMapping("/panel")
    String panel() {
        return "gestion/panel";
    }
}
