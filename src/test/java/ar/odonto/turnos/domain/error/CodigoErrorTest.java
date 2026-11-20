package ar.odonto.turnos.domain.error;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Spec base-del-sistema: "Catálogo de códigos de error con estado HTTP fijo".
 */
class CodigoErrorTest {

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
            "CHOQUE_PROFESIONAL, 409", "CHOQUE_SILLON, 409", "CHOQUE_PACIENTE, 409", "DATO_DUPLICADO, 409",
            "RECURSO_INEXISTENTE, 404",
            "ENTRADA_INVALIDA, 400",
            "NO_AUTENTICADO, 401", "ACCESO_DENEGADO, 403",
            "SILLON_REQUERIDO, 422", "TURNO_EN_PASADO, 422", "FUERA_DE_HORARIO, 422", "RECURSO_INACTIVO, 422",
            "CANCELACION_TARDIA, 422", "TURNO_YA_CANCELADO, 422", "PACIENTE_SIN_TELEFONO, 422",
            "DURACION_INVALIDA, 422", "HORARIO_INVALIDO, 422",
            "ERROR_INTERNO, 500"
    })
    void cadaCodigoTieneSuEstadoHttp(CodigoError codigo, int estadoEsperado) {
        assertThat(codigo.estadoHttp()).isEqualTo(estadoEsperado);
    }

    @Test
    void elCatalogoContieneTodosLosCodigosDeLaSpec() {
        assertThat(Arrays.stream(CodigoError.values()).map(Enum::name)).contains(
                "CHOQUE_PROFESIONAL", "CHOQUE_SILLON", "SILLON_REQUERIDO", "CHOQUE_PACIENTE", "TURNO_EN_PASADO",
                "FUERA_DE_HORARIO", "RECURSO_INEXISTENTE", "RECURSO_INACTIVO", "CANCELACION_TARDIA",
                "TURNO_YA_CANCELADO", "PACIENTE_SIN_TELEFONO", "ENTRADA_INVALIDA", "DATO_DUPLICADO",
                "DURACION_INVALIDA", "HORARIO_INVALIDO", "NO_AUTENTICADO", "ACCESO_DENEGADO", "ERROR_INTERNO");
    }

    @Test
    void todoCodigoTieneTitulo() {
        assertThat(CodigoError.values()).allSatisfy(codigo -> assertThat(codigo.titulo()).isNotBlank());
    }
}
