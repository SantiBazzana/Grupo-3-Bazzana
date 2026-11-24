package ar.odonto.turnos.web.view.gestion;

import ar.odonto.turnos.application.pacientes.AltaPaciente;
import ar.odonto.turnos.application.pacientes.ListarPacientes;
import ar.odonto.turnos.domain.error.ReglaDeNegocioException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Pantalla de pacientes de C-09: consume los casos de uso de C-04 (DNI único y búsqueda
 * resueltos ahí). La vista solo traduce formularios y muestra el {@code codigo} del error.
 */
@Controller
@RequestMapping("/gestion/pacientes")
class PacienteViewController {

    private static final String VISTA = "gestion/pacientes";

    private final ListarPacientes listarPacientes;
    private final AltaPaciente altaPaciente;

    PacienteViewController(ListarPacientes listarPacientes, AltaPaciente altaPaciente) {
        this.listarPacientes = listarPacientes;
        this.altaPaciente = altaPaciente;
    }

    @GetMapping
    String listar(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("pacientes", listarPacientes.ejecutar(q));
        model.addAttribute("q", q == null ? "" : q);
        return VISTA;
    }

    @PostMapping
    String crear(@RequestParam String apellido, @RequestParam String nombre, @RequestParam String dni,
                 @RequestParam(required = false) String telefono, @RequestParam(required = false) String email,
                 Model model) {
        try {
            altaPaciente.ejecutar(new AltaPaciente.Comando(nombre, apellido, dni, telefono, email));
            return "redirect:/gestion/pacientes";
        } catch (ReglaDeNegocioException ex) {
            model.addAttribute("errorCodigo", ex.codigo().name());
            model.addAttribute("errorMensaje", ex.getMessage());
            model.addAttribute("pacientes", listarPacientes.ejecutar(null));
            model.addAttribute("q", "");
            return VISTA;
        }
    }
}
