package com.mantasguajiras.backend.common.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

/** Periodo de los historiales: por defecto 30 días, máximo 93. */
class DateRangeTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 24);

    @Test
    void sinFechasSonLosUltimos30DiasConHoyIncluido() {
        DateRange range = DateRange.resolve(null, null, TODAY);
        assertThat(range.start()).isEqualTo(LocalDate.of(2026, 10, 26).atStartOfDay());
        assertThat(range.endExclusive()).isEqualTo(LocalDate.of(2026, 11, 25).atStartOfDay());
    }

    @Test
    void soloHastaSonLos30DiasQueTerminanEseDia() {
        DateRange range = DateRange.resolve(null, LocalDate.of(2026, 3, 31), TODAY);
        assertThat(range.start()).isEqualTo(LocalDate.of(2026, 3, 2).atStartOfDay());
        assertThat(range.endExclusive()).isEqualTo(LocalDate.of(2026, 4, 1).atStartOfDay());
    }

    @Test
    void soloDesdeEsHastaHoy() {
        DateRange range = DateRange.resolve(LocalDate.of(2026, 11, 1), null, TODAY);
        assertThat(range.start()).isEqualTo(LocalDate.of(2026, 11, 1).atStartOfDay());
        assertThat(range.endExclusive()).isEqualTo(LocalDate.of(2026, 11, 25).atStartOfDay());
    }

    @Test
    void soloDesdeUnDiaFuturoEsUnPeriodoVacioNoUnError() {
        DateRange range = DateRange.resolve(TODAY.plusDays(1), null, TODAY);
        assertThat(range.start()).isEqualTo(TODAY.plusDays(1).atStartOfDay());
    }

    @Test
    void unSoloDia() {
        DateRange range = DateRange.resolve(TODAY, TODAY, TODAY);
        assertThat(range.endExclusive()).isEqualTo(range.start().plusDays(1));
    }

    @Test
    void hasta93DiasSePermite() {
        LocalDate from = TODAY.minusDays(DateRange.MAX_DAYS - 1L);
        assertThat(DateRange.resolve(from, TODAY, TODAY).start()).isEqualTo(from.atStartOfDay());
    }

    @Test
    void masDe93DiasSeRechazaConMensajeClaro() {
        assertThatThrownBy(() -> DateRange.resolve(TODAY.minusDays(DateRange.MAX_DAYS), TODAY, TODAY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("máximo 93 días");
        assertThatThrownBy(() -> DateRange.resolve(LocalDate.of(2025, 1, 1), null, TODAY))
                .hasMessageContaining("máximo 93 días");
    }

    @Test
    void desdeDespuesDeHastaSeRechaza() {
        assertThatThrownBy(() -> DateRange.resolve(TODAY, TODAY.minusDays(1), TODAY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("posterior");
    }
}
