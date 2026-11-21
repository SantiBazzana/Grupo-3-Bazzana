package ar.odonto.turnos.application.configuracion;

import ar.odonto.turnos.domain.model.Profesional;
import ar.odonto.turnos.domain.port.ProfesionalRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/** Verifica la decisión (d): la edición nunca consulta unicidad de matrícula. */
class EditarProfesionalConContadorTest {

    /** Fake con estado y contador, propio de C-03 (no se toca el fixture compartido). */
    static class RepositorioConContador implements ProfesionalRepository {

        final Profesional profesional = new Profesional("Ana", "Ejemplo", "M-0001", "5491100000001");
        int consultasDeUnicidad;

        @Override
        public Profesional save(Profesional entidad) {
            return entidad;
        }

        @Override
        public Optional<Profesional> findById(Long id) {
            return id == 1L ? Optional.of(profesional) : Optional.empty();
        }

        @Override
        public List<Profesional> findAll() {
            return List.of(profesional);
        }

        @Override
        public long count() {
            return 1;
        }

        @Override
        public boolean existsByMatricula(String matricula) {
            consultasDeUnicidad++;
            return false;
        }
    }

    @Test
    void editarMismaMatriculaNoConsultaUnicidad() {
        RepositorioConContador repositorio = new RepositorioConContador();

        new EditarProfesional(repositorio).ejecutar(
                new EditarProfesional.Comando(1L, null, null, "M-0001", null, false));

        assertThat(repositorio.consultasDeUnicidad).isZero();
        assertThat(repositorio.profesional.isActivo()).isFalse();
    }

    @Test
    void editarSinMatriculaNoConsultaUnicidad() {
        RepositorioConContador repositorio = new RepositorioConContador();

        new EditarProfesional(repositorio).ejecutar(
                new EditarProfesional.Comando(1L, "AnaMaria", null, null, null, null));

        assertThat(repositorio.consultasDeUnicidad).isZero();
        assertThat(repositorio.profesional.getNombre()).isEqualTo("AnaMaria");
    }

    @Test
    void matriculaDistintaTampocoConsultaUnicidad() {
        RepositorioConContador repositorio = new RepositorioConContador();

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                new EditarProfesional(repositorio).ejecutar(
                        new EditarProfesional.Comando(1L, null, null, "M-0099", null, null)))
                .isInstanceOf(ar.odonto.turnos.domain.error.ReglaDeNegocioException.class);

        assertThat(repositorio.consultasDeUnicidad).isZero();
        assertThat(repositorio.profesional.getMatricula()).isEqualTo("M-0001");
    }
}
