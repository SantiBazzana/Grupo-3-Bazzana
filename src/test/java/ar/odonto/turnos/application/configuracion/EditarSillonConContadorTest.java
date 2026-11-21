package ar.odonto.turnos.application.configuracion;

import ar.odonto.turnos.domain.model.Sillon;
import ar.odonto.turnos.domain.port.SillonRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/** Verifica la decisión (d): no se consulta unicidad cuando el nombre no cambia. */
class EditarSillonConContadorTest {

    /** Fake con estado y contador, propio de C-03 (no se toca el fixture compartido). */
    static class RepositorioConContador implements SillonRepository {

        final Sillon sillon = new Sillon("Sillon 1");
        int consultasDeUnicidad;

        @Override
        public Sillon save(Sillon entidad) {
            return entidad;
        }

        @Override
        public Optional<Sillon> findById(Long id) {
            return id == 1L ? Optional.of(sillon) : Optional.empty();
        }

        @Override
        public List<Sillon> findAll() {
            return List.of(sillon);
        }

        @Override
        public long count() {
            return 1;
        }

        @Override
        public boolean existsByNombre(String nombre) {
            consultasDeUnicidad++;
            return false;
        }
    }

    @Test
    void editarMismoNombreNoConsultaUnicidad() {
        RepositorioConContador repositorio = new RepositorioConContador();

        new EditarSillon(repositorio).ejecutar(new EditarSillon.Comando(1L, "Sillon 1", false));

        assertThat(repositorio.consultasDeUnicidad).isZero();
        assertThat(repositorio.sillon.isActivo()).isFalse();
    }

    @Test
    void editarSoloActivoNoConsultaUnicidad() {
        RepositorioConContador repositorio = new RepositorioConContador();

        new EditarSillon(repositorio).ejecutar(new EditarSillon.Comando(1L, null, false));

        assertThat(repositorio.consultasDeUnicidad).isZero();
        assertThat(repositorio.sillon.getNombre()).isEqualTo("Sillon 1");
    }

    @Test
    void editarNombreDistintoSiConsultaUnicidad() {
        RepositorioConContador repositorio = new RepositorioConContador();

        new EditarSillon(repositorio).ejecutar(new EditarSillon.Comando(1L, "Sillon 2", null));

        assertThat(repositorio.consultasDeUnicidad).isEqualTo(1);
        assertThat(repositorio.sillon.getNombre()).isEqualTo("Sillon 2");
    }
}
