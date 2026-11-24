package ar.odonto.turnos.web.view.turnos;

import ar.odonto.turnos.application.agenda.ConsultarAgenda;
import ar.odonto.turnos.application.configuracion.ListarProfesionales;
import ar.odonto.turnos.application.configuracion.ProfesionalVista;
import ar.odonto.turnos.domain.error.CodigoError;
import ar.odonto.turnos.domain.error.ReglaDeNegocioException;
import ar.odonto.turnos.domain.model.Solicitante;
import ar.odonto.turnos.domain.port.UsuarioActual;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Vista "Mi agenda" del día (C-10). Obtiene el {@link Solicitante} del puerto {@link UsuarioActual} y delega la
 * regla de visibilidad RN-AG-02 en {@link ConsultarAgenda} (D3): la vista solo adapta entrada y salida.
 */
@Controller
@RequestMapping("/agenda")
class AgendaViewController {

    private static final String VISTA = "agenda/dia";
    private static final DateTimeFormatter FECHA_TITULO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ConsultarAgenda consultarAgenda;
    private final ListarProfesionales listarProfesionales;
    private final UsuarioActual usuarioActual;
    private final Clock reloj;

    AgendaViewController(ConsultarAgenda consultarAgenda, ListarProfesionales listarProfesionales,
            UsuarioActual usuarioActual, Clock reloj) {
        this.consultarAgenda = consultarAgenda;
        this.listarProfesionales = listarProfesionales;
        this.usuarioActual = usuarioActual;
        this.reloj = reloj;
    }

    @GetMapping
    String dia(@RequestParam(required = false) String fecha,
            @RequestParam(required = false) Long profesionalId, Model model, HttpServletResponse response) {
        Solicitante solicitante = usuarioActual.solicitante();
        LocalDate dia = fecha(fecha, model);
        Long profesional = profesional(solicitante, profesionalId);

        List<FilaDeAgenda> filas = List.of();
        if (profesional != null) {
            try {
                filas = consultarAgenda.ejecutar(profesional, dia, solicitante).stream()
                        .map(FilaDeAgenda::de)
                        .toList();
            } catch (ReglaDeNegocioException ex) {
                if (ex.codigo() == CodigoError.ACCESO_DENEGADO) {
                    return AccesoDenegado.vista(response, model);
                }
                throw ex;
            }
        }

        model.addAttribute("filas", filas);
        model.addAttribute("fecha", dia.toString());
        model.addAttribute("fechaTitulo", FECHA_TITULO.format(dia));
        model.addAttribute("profesionales", profesionalesActivos());
        model.addAttribute("profesionalId", profesional);
        model.addAttribute("profesionalNombre", nombreProfesional(profesional));
        model.addAttribute("esOdontologo", solicitante.esOdontologo());
        return VISTA;
    }

    private LocalDate fecha(String fecha, Model model) {
        if (fecha == null || fecha.isBlank()) {
            return LocalDate.now(reloj);
        }
        try {
            return LocalDate.parse(fecha);
        } catch (DateTimeException ex) {
            model.addAttribute("errorCodigo", CodigoError.ENTRADA_INVALIDA.name());
            model.addAttribute("errorMensaje", "La fecha debe tener el formato AAAA-MM-DD");
            return LocalDate.now(reloj);
        }
    }

    /**
     * ADMIN/SECRETARIA eligen profesional; si no vienen parámetros se usa el primero activo. Un ODONTOLOGO
     * sin parámetro ve el suyo (RN-AG-02); con un parámetro ajeno, {@link ConsultarAgenda} deniega el acceso.
     */
    private Long profesional(Solicitante solicitante, Long profesionalId) {
        if (profesionalId != null) {
            return profesionalId;
        }
        if (solicitante.esOdontologo()) {
            return solicitante.profesionalId();
        }
        return profesionalesActivos().stream().findFirst().map(ProfesionalVista::id).orElse(null);
    }

    private List<ProfesionalVista> profesionalesActivos() {
        return listarProfesionales.ejecutar().stream().filter(ProfesionalVista::activo).toList();
    }

    private String nombreProfesional(Long id) {
        if (id == null) {
            return "—";
        }
        return listarProfesionales.ejecutar().stream()
                .filter(p -> p.id().equals(id))
                .findFirst()
                .map(p -> p.nombre() + " " + p.apellido())
                .orElse("—");
    }
}
