package ar.odonto.turnos.web.view.gestion;

import ar.odonto.turnos.application.configuracion.AltaProfesional;
import ar.odonto.turnos.application.configuracion.ListarProfesionales;
import ar.odonto.turnos.domain.error.ReglaDeNegocioException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Pantalla de profesionales de C-09. Solo traduce el formulario a los comandos de C-03:
 * las reglas (matrícula única, trim) viven en los casos de uso.
 */
@Controller
@RequestMapping("/gestion/profesionales")
class ProfesionalViewController {

    private static final String VISTA = "gestion/profesionales";

    private final ListarProfesionales listarProfesionales;
    private final AltaProfesional altaProfesional;

    ProfesionalViewController(ListarProfesionales listarProfesionales, AltaProfesional altaProfesional) {
        this.listarProfesionales = listarProfesionales;
        this.altaProfesional = altaProfesional;
    }

    @GetMapping
    String listar(Model model) {
        model.addAttribute("profesionales", listarProfesionales.ejecutar());
        return VISTA;
    }

    @PostMapping
    String crear(@RequestParam String nombre, @RequestParam String apellido, @RequestParam String matricula,
                 @RequestParam(required = false) String telefono, Model model) {
        try {
            altaProfesional.ejecutar(new AltaProfesional.Comando(nombre, apellido, matricula, telefono));
            return "redirect:/gestion/profesionales";
        } catch (ReglaDeNegocioException ex) {
            return mostrarError(model, ex);
        }
    }

    private String mostrarError(Model model, ReglaDeNegocioException ex) {
        model.addAttribute("errorCodigo", ex.codigo().name());
        model.addAttribute("errorMensaje", ex.getMessage());
        model.addAttribute("profesionales", listarProfesionales.ejecutar());
        return VISTA;
    }
}
