package ar.odonto.turnos.infrastructure.persistence;

import ar.odonto.turnos.domain.model.Paciente;
import ar.odonto.turnos.domain.model.Prestacion;
import ar.odonto.turnos.domain.model.Profesional;
import ar.odonto.turnos.domain.model.Sillon;
import ar.odonto.turnos.domain.model.Turno;
import ar.odonto.turnos.domain.port.PacienteRepository;
import ar.odonto.turnos.domain.port.PrestacionRepository;
import ar.odonto.turnos.domain.port.ProfesionalRepository;
import ar.odonto.turnos.domain.port.SillonRepository;
import ar.odonto.turnos.domain.port.TurnoRepository;
import ar.odonto.turnos.support.DatosDePrueba;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tareas 4.3 y 4.4 de C-01: consultas de choque y de agenda (design D4).
 * Solo se prueba la CONSULTA; la regla de negocio y su orden de validación son de C-02.
 * Escenario base: existe un turno 09:30-10:00 con Ana en el Sillón 1, margen de 5 minutos.
 */
@DataJpaTest
class TurnoRepositoryChoquesTest {

    private static final Duration MARGEN = Duration.ofMinutes(5);

    @Autowired TurnoRepository turnos;
    @Autowired ProfesionalRepository profesionales;
    @Autowired SillonRepository sillones;
    @Autowired PacienteRepository pacientes;
    @Autowired PrestacionRepository prestaciones;

    Profesional ana;
    Profesional bruno;
    Sillon sillon1;
    Sillon sillon2;
    Paciente paciente1;
    Paciente paciente2;
    Prestacion control;
    Turno existente;

    @BeforeEach
    void datos() {
        ana = profesionales.save(DatosDePrueba.profesional("MP-0001"));
        bruno = profesionales.save(DatosDePrueba.profesional("MP-0002"));
        sillon1 = sillones.save(DatosDePrueba.sillon("Sillón 1"));
        sillon2 = sillones.save(DatosDePrueba.sillon("Sillón 2"));
        paciente1 = pacientes.save(DatosDePrueba.paciente(1));
        paciente2 = pacientes.save(DatosDePrueba.paciente(2));
        control = prestaciones.save(DatosDePrueba.prestacion("Control", 30));
        existente = turnos.save(DatosDePrueba.turno(ana, sillon1, paciente1, control,
                "2026-11-23T09:30", "2026-11-23T10:00"));
    }

    /** Ventana con margen para un turno nuevo [inicio, inicio + 30 min). */
    private static LocalDateTime desde(String inicio) {
        return LocalDateTime.parse(inicio).minus(MARGEN);
    }

    private static LocalDateTime hasta(String inicio) {
        return LocalDateTime.parse(inicio).plusMinutes(30).plus(MARGEN);
    }

    private List<Turno> choquesSillon1(String inicio) {
        return turnos.buscarChoquesDeSillon(sillon1.getId(), desde(inicio), hasta(inicio), null);
    }

    private List<Turno> choquesAna(String inicio) {
        return turnos.buscarChoquesDeProfesional(ana.getId(), desde(inicio), hasta(inicio), null);
    }

    @Test
    void borde_turnoQueEmpieza5MinutosDespues_noChoca() {
        assertThat(choquesSillon1("2026-11-23T10:05")).isEmpty();
        assertThat(choquesAna("2026-11-23T10:05")).isEmpty();
    }

    @Test
    void borde_turnoQueEmpieza4MinutosDespues_choca() {
        assertThat(choquesSillon1("2026-11-23T10:04")).containsExactly(existente);
        assertThat(choquesAna("2026-11-23T10:04")).containsExactly(existente);
    }

    @Test
    void borde_turnoQueTerminaJusto5MinutosAntes_noChoca() {
        // nuevo 08:55-09:25: termina 5 min antes de que empiece el existente (09:30)
        assertThat(choquesSillon1("2026-11-23T08:55")).isEmpty();
        // nuevo 08:56-09:26: termina 4 min antes → choca
        assertThat(choquesSillon1("2026-11-23T08:56")).containsExactly(existente);
    }

    @Test
    void superposicionTotal_choca() {
        assertThat(choquesSillon1("2026-11-23T09:40")).containsExactly(existente);
    }

    @Test
    void otroSillonYOtroProfesional_noChocan() {
        assertThat(turnos.buscarChoquesDeSillon(sillon2.getId(), desde("2026-11-23T09:40"), hasta("2026-11-23T09:40"),
                null)).isEmpty();
        assertThat(turnos.buscarChoquesDeProfesional(bruno.getId(), desde("2026-11-23T09:40"),
                hasta("2026-11-23T09:40"), null)).isEmpty();
    }

    @Test
    void turnoCancelado_nuncaApareceComoChoque() {
        existente.marcarCancelado(LocalDateTime.parse("2026-11-22T10:00"));
        turnos.save(existente);

        assertThat(choquesSillon1("2026-11-23T09:40")).isEmpty();
        assertThat(choquesAna("2026-11-23T09:40")).isEmpty();
        assertThat(turnos.buscarSuperpuestosDePaciente(paciente1.getId(),
                LocalDateTime.parse("2026-11-23T09:40"), LocalDateTime.parse("2026-11-23T10:10"), null)).isEmpty();
    }

    @Test
    void excluirTurnoId_excluyeEseTurno() {
        assertThat(turnos.buscarChoquesDeSillon(sillon1.getId(), desde("2026-11-23T09:40"), hasta("2026-11-23T09:40"),
                existente.getId())).isEmpty();
    }

    @Test
    void paciente_seSuperponeSinMargen() {
        LocalDateTime finExistente = LocalDateTime.parse("2026-11-23T10:00");
        // empieza justo cuando termina el otro: sin margen no se superpone
        assertThat(turnos.buscarSuperpuestosDePaciente(paciente1.getId(), finExistente, finExistente.plusMinutes(30),
                null)).isEmpty();
        // empieza 1 minuto antes de que termine: se superpone
        assertThat(turnos.buscarSuperpuestosDePaciente(paciente1.getId(), finExistente.minusMinutes(1),
                finExistente.plusMinutes(29), null)).containsExactly(existente);
        // otro paciente no se ve afectado
        assertThat(turnos.buscarSuperpuestosDePaciente(paciente2.getId(), finExistente.minusMinutes(1),
                finExistente.plusMinutes(29), null)).isEmpty();
    }

    @Test
    void agenda_devuelveSoloActivosDelRangoOrdenadosPorInicio() {
        Turno tarde = turnos.save(DatosDePrueba.turno(ana, sillon2, paciente2, control,
                "2026-11-23T15:00", "2026-11-23T15:30"));
        Turno temprano = turnos.save(DatosDePrueba.turno(ana, sillon2, paciente2, control,
                "2026-11-23T08:00", "2026-11-23T08:30"));
        Turno cancelado = turnos.save(DatosDePrueba.turno(ana, sillon2, paciente2, control,
                "2026-11-23T12:00", "2026-11-23T12:30"));
        cancelado.marcarCancelado(LocalDateTime.parse("2026-11-22T09:00"));
        turnos.save(cancelado);
        turnos.save(DatosDePrueba.turno(ana, sillon2, paciente2, control,
                "2026-11-24T08:00", "2026-11-24T08:30")); // otro día
        turnos.save(DatosDePrueba.turno(bruno, sillon2, paciente2, control,
                "2026-11-23T11:00", "2026-11-23T11:30")); // otro profesional

        List<Turno> agenda = turnos.buscarActivosDeProfesionalEntre(ana.getId(),
                LocalDateTime.parse("2026-11-23T00:00"), LocalDateTime.parse("2026-11-24T00:00"));

        assertThat(agenda).containsExactly(temprano, existente, tarde);
    }
}
