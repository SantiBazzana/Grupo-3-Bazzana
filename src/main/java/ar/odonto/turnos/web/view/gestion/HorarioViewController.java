package ar.odonto.turnos.web.view.gestion;

import ar.odonto.turnos.application.configuracion.ConsultarHorario;
import ar.odonto.turnos.application.configuracion.ReemplazarHorario;
import ar.odonto.turnos.domain.error.ReglaDeNegocioException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Pantalla de horario de atención de C-09. Una franja por día: el formulario manda el día y,
 * si apertura y cierre vienen vacíos, el caso de uso de C-03 cierra el día.
 */
@Controller
@RequestMapping("/gestion/horario")
class HorarioViewController {

    private static final String VISTA = "gestion/horario";

    private static final Map<DayOfWeek, String> NOMBRES_DIA = nombresEnEspanol();

    private final ConsultarHorario consultarHorario;
    private final ReemplazarHorario reemplazarHorario;

    HorarioViewController(ConsultarHorario consultarHorario, ReemplazarHorario reemplazarHorario) {
        this.consultarHorario = consultarHorario;
        this.reemplazarHorario = reemplazarHorario;
    }

    @GetMapping
    String listar(Model model) {
        model.addAttribute("franjas", consultarHorario.ejecutar());
        model.addAttribute("nombresDia", NOMBRES_DIA);
        return VISTA;
    }

    @PostMapping
    String reemplazar(@RequestParam String diaSemana, @RequestParam(required = false) String apertura,
                      @RequestParam(required = false) String cierre, Model model) {
        try {
            reemplazarHorario.ejecutar(new ReemplazarHorario.Comando(DayOfWeek.valueOf(diaSemana),
                    hora(apertura), hora(cierre)));
            return "redirect:/gestion/horario";
        } catch (ReglaDeNegocioException ex) {
            model.addAttribute("errorCodigo", ex.codigo().name());
            model.addAttribute("errorMensaje", ex.getMessage());
            model.addAttribute("franjas", consultarHorario.ejecutar());
            model.addAttribute("nombresDia", NOMBRES_DIA);
            return VISTA;
        }
    }

    private static LocalTime hora(String valor) {
        return valor == null || valor.isBlank() ? null : LocalTime.parse(valor);
    }

    private static Map<DayOfWeek, String> nombresEnEspanol() {
        Map<DayOfWeek, String> nombres = new LinkedHashMap<>();
        nombres.put(DayOfWeek.MONDAY, "Lunes");
        nombres.put(DayOfWeek.TUESDAY, "Martes");
        nombres.put(DayOfWeek.WEDNESDAY, "Miércoles");
        nombres.put(DayOfWeek.THURSDAY, "Jueves");
        nombres.put(DayOfWeek.FRIDAY, "Viernes");
        nombres.put(DayOfWeek.SATURDAY, "Sábado");
        nombres.put(DayOfWeek.SUNDAY, "Domingo");
        return Map.copyOf(nombres);
    }
}
