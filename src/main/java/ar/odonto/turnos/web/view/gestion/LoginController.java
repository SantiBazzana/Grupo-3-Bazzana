package ar.odonto.turnos.web.view.gestion;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Vista propia de login (C-09). El POST lo procesa el filtro de form login de Spring
 * Security configurado en C-05; acá solo se sirve el formulario con su token CSRF.
 */
@Controller
class LoginController {

    @GetMapping("/login")
    String login() {
        return "login";
    }
}
