package ar.odonto.turnos.domain.error;

import java.util.Objects;

/**
 * Única excepción que lanzan las reglas de negocio. El manejador global la traduce a Problem Details
 * con el campo {@code codigo}. El detalle nunca debe incluir DNI, teléfono ni nombre completo de pacientes (D1/D2).
 */
public class ReglaDeNegocioException extends RuntimeException {

    private final CodigoError codigo;

    public ReglaDeNegocioException(CodigoError codigo, String detalle) {
        super(detalle);
        this.codigo = Objects.requireNonNull(codigo, "codigo");
    }

    public ReglaDeNegocioException(CodigoError codigo) {
        this(codigo, codigo.titulo());
    }

    public CodigoError codigo() {
        return codigo;
    }
}
