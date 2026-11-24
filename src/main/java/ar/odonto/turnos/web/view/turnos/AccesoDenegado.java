package ar.odonto.turnos.web.view.turnos;

import ar.odonto.turnos.domain.error.CodigoError;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ui.Model;

/**
 * Traduce una denegación de las vistas de turnos/agenda a una respuesta HTML 403 con el código de
 * {@link CodigoError}. Se usa en el controlador (D4 de C-09): no edita `SeguridadConfig`, que es compartido.
 */
final class AccesoDenegado {

    private AccesoDenegado() {
    }

    static String vista(HttpServletResponse response, Model model) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        model.addAttribute("errorCodigo", CodigoError.ACCESO_DENEGADO.name());
        model.addAttribute("errorMensaje", CodigoError.ACCESO_DENEGADO.titulo());
        return "turnos/acceso-denegado";
    }
}
