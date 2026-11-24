package ar.odonto.turnos.web.view.turnos;

import ar.odonto.turnos.application.aviso.GenerarLinkWhatsApp;
import ar.odonto.turnos.application.cancelacion.CancelarTurno;
import ar.odonto.turnos.application.configuracion.ListarPrestaciones;
import ar.odonto.turnos.application.configuracion.ListarProfesionales;
import ar.odonto.turnos.application.configuracion.ListarSillones;
import ar.odonto.turnos.application.configuracion.PrestacionVista;
import ar.odonto.turnos.application.configuracion.ProfesionalVista;
import ar.odonto.turnos.application.configuracion.SillonVista;
import ar.odonto.turnos.application.pacientes.ListarPacientes;
import ar.odonto.turnos.application.pacientes.PacienteVista;
import ar.odonto.turnos.application.turnos.ConsultarTurno;
import ar.odonto.turnos.application.turnos.CrearTurno;
import ar.odonto.turnos.application.turnos.TurnoVista;
import ar.odonto.turnos.domain.error.CodigoError;
import ar.odonto.turnos.domain.error.ReglaDeNegocioException;
import ar.odonto.turnos.domain.model.Solicitante;
import ar.odonto.turnos.domain.port.UsuarioActual;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Vistas de turnos de C-10 (US-010, US-011, US-030). Consume los casos de uso existentes (D1): no llama a la
 * API REST, no duplica reglas y no expone entidades JPA. La autorización de acciones sensibles se resuelve
 * acá con el {@link Solicitante} del puerto {@link UsuarioActual}, sin editar `SeguridadConfig` (D4).
 */
@Controller
@RequestMapping("/turnos")
class TurnoViewController {

    private static final String VISTA_NUEVO = "turnos/nuevo";
    private static final String VISTA_DETALLE = "turnos/detalle";
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy · HH:mm");

    private final CrearTurno crearTurno;
    private final ConsultarTurno consultarTurno;
    private final CancelarTurno cancelarTurno;
    private final GenerarLinkWhatsApp generarLinkWhatsApp;
    private final ListarPacientes listarPacientes;
    private final ListarProfesionales listarProfesionales;
    private final ListarSillones listarSillones;
    private final ListarPrestaciones listarPrestaciones;
    private final UsuarioActual usuarioActual;
    private final Clock reloj;

    TurnoViewController(CrearTurno crearTurno, ConsultarTurno consultarTurno, CancelarTurno cancelarTurno,
            GenerarLinkWhatsApp generarLinkWhatsApp, ListarPacientes listarPacientes,
            ListarProfesionales listarProfesionales, ListarSillones listarSillones,
            ListarPrestaciones listarPrestaciones, UsuarioActual usuarioActual, Clock reloj) {
        this.crearTurno = crearTurno;
        this.consultarTurno = consultarTurno;
        this.cancelarTurno = cancelarTurno;
        this.generarLinkWhatsApp = generarLinkWhatsApp;
        this.listarPacientes = listarPacientes;
        this.listarProfesionales = listarProfesionales;
        this.listarSillones = listarSillones;
        this.listarPrestaciones = listarPrestaciones;
        this.usuarioActual = usuarioActual;
        this.reloj = reloj;
    }

    @GetMapping("/nuevo")
    String nuevo(Model model, HttpServletResponse response) {
        if (esOdontologo()) {
            return AccesoDenegado.vista(response, model);
        }
        cargarOpciones(model, formularioPorDefecto());
        return VISTA_NUEVO;
    }

    @PostMapping("/nuevo")
    String crear(@RequestParam Long pacienteId, @RequestParam Long profesionalId, @RequestParam Long sillonId,
            @RequestParam Long prestacionId, @RequestParam(required = false) String inicio,
            @RequestParam(required = false, defaultValue = "false") boolean sobreturno,
            Model model, HttpServletResponse response) {
        if (esOdontologo()) {
            return AccesoDenegado.vista(response, model);
        }
        Map<String, String> form = formulario(pacienteId, profesionalId, sillonId, prestacionId, inicio, sobreturno);
        if (inicio == null || inicio.isBlank()) {
            cargarConError(model, form, CodigoError.ENTRADA_INVALIDA.name(),
                    "La fecha y hora de inicio son obligatorias");
            return VISTA_NUEVO;
        }
        try {
            TurnoVista creado = crearTurno.ejecutar(new CrearTurno.Comando(pacienteId, profesionalId, sillonId,
                    prestacionId, LocalDateTime.parse(inicio), sobreturno));
            return "redirect:/agenda?fecha=" + creado.inicio().toLocalDate()
                    + "&profesionalId=" + creado.profesionalId();
        } catch (DateTimeException ex) {
            cargarConError(model, form, CodigoError.ENTRADA_INVALIDA.name(),
                    "La fecha y hora de inicio deben tener el formato AAAA-MM-DDTHH:mm");
            return VISTA_NUEVO;
        } catch (ReglaDeNegocioException ex) {
            cargarConError(model, form, ex.codigo().name(), ex.getMessage());
            return VISTA_NUEVO;
        }
    }

    @GetMapping("/{id}")
    String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("turno", detalleDe(id));
        cargarWhatsapp(model, id);
        return VISTA_DETALLE;
    }

    @PostMapping("/{id}/cancelar")
    String cancelar(@PathVariable Long id, Model model, HttpServletResponse response) {
        if (esOdontologo()) {
            return AccesoDenegado.vista(response, model);
        }
        try {
            cancelarTurno.ejecutar(id);
            return "redirect:/turnos/" + id;
        } catch (ReglaDeNegocioException ex) {
            model.addAttribute("turno", detalleDe(id));
            cargarWhatsapp(model, id);
            model.addAttribute("errorCodigo", ex.codigo().name());
            model.addAttribute("errorMensaje", ex.getMessage());
            return VISTA_DETALLE;
        }
    }

    private boolean esOdontologo() {
        return usuarioActual.solicitante().esOdontologo();
    }

    private void cargarConError(Model model, Map<String, String> form, String codigo, String mensaje) {
        cargarOpciones(model, form);
        model.addAttribute("errorCodigo", codigo);
        model.addAttribute("errorMensaje", mensaje);
    }

    private void cargarOpciones(Model model, Map<String, String> form) {
        List<PacienteVista> pacientes = listarPacientes.ejecutar(null);
        List<ProfesionalVista> profesionales = listarProfesionales.ejecutar().stream()
                .filter(ProfesionalVista::activo).toList();
        List<SillonVista> sillones = listarSillones.ejecutar().stream().filter(SillonVista::activo).toList();
        List<PrestacionVista> prestaciones = listarPrestaciones.ejecutar().stream()
                .filter(PrestacionVista::activa).toList();
        model.addAttribute("pacientes", pacientes);
        model.addAttribute("profesionales", profesionales);
        model.addAttribute("sillones", sillones);
        model.addAttribute("prestaciones", prestaciones);
        model.addAttribute("form", form);
    }

    private Map<String, String> formularioPorDefecto() {
        return formulario(null, null, null, null, LocalDate.now(reloj).atTime(9, 0).toString(), false);
    }

    private static Map<String, String> formulario(Long pacienteId, Long profesionalId, Long sillonId,
            Long prestacionId, String inicio, boolean sobreturno) {
        Map<String, String> form = new LinkedHashMap<>();
        form.put("pacienteId", texto(pacienteId));
        form.put("profesionalId", texto(profesionalId));
        form.put("sillonId", texto(sillonId));
        form.put("prestacionId", texto(prestacionId));
        form.put("inicio", inicio == null ? "" : inicio);
        form.put("sobreturno", Boolean.toString(sobreturno));
        return form;
    }

    private static String texto(Long valor) {
        return valor == null ? "" : valor.toString();
    }

    private DetalleDeTurno detalleDe(Long id) {
        TurnoVista turno = consultarTurno.ejecutar(id);
        return new DetalleDeTurno(turno.id(), nombrePaciente(turno.pacienteId()),
                nombreProfesional(turno.profesionalId()), nombreSillon(turno.sillonId()),
                nombrePrestacion(turno.prestacionId()), FORMATO.format(turno.inicio()), FORMATO.format(turno.fin()),
                turno.estado().name(), turno.sobreturno(), turno.creadoPor(), null, false);
    }

    /** {@code PACIENTE_SIN_TELEFONO} es un estado de la vista, nunca una alerta de error de página (D2). */
    private void cargarWhatsapp(Model model, Long id) {
        if (esOdontologo()) {
            return;
        }
        try {
            model.addAttribute("whatsappUrl", generarLinkWhatsApp.ejecutar(id).url());
        } catch (ReglaDeNegocioException ex) {
            if (ex.codigo() != CodigoError.PACIENTE_SIN_TELEFONO) {
                throw ex;
            }
            model.addAttribute("sinTelefono", true);
        }
    }

    private String nombrePaciente(Long id) {
        return listarPacientes.ejecutar(null).stream()
                .filter(p -> p.id().equals(id))
                .findFirst()
                .map(p -> p.nombre() + " " + p.apellido())
                .orElse("—");
    }

    private String nombreProfesional(Long id) {
        return listarProfesionales.ejecutar().stream()
                .filter(p -> p.id().equals(id))
                .findFirst()
                .map(p -> p.nombre() + " " + p.apellido())
                .orElse("—");
    }

    private String nombreSillon(Long id) {
        return listarSillones.ejecutar().stream()
                .filter(s -> s.id().equals(id))
                .findFirst()
                .map(SillonVista::nombre)
                .orElse("—");
    }

    private String nombrePrestacion(Long id) {
        return listarPrestaciones.ejecutar().stream()
                .filter(p -> p.id().equals(id))
                .findFirst()
                .map(PrestacionVista::nombre)
                .orElse("—");
    }
}
