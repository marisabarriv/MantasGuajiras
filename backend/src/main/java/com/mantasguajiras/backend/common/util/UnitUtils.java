package com.mantasguajiras.backend.common.util;

import com.mantasguajiras.backend.unit.entity.Unit;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Reglas de unidades de MantasGuajiras:
 * - Mantas: unidad "#" → solo cantidades enteras.
 * - Telas: metros → admiten decimales.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UnitUtils {

    /** true si la unidad se cuenta en enteros (unidad "#"). */
    public static boolean isWholeUnit(Unit unit) {

        if (unit == null) {
            return false;
        }

        String abbreviation = unit.getAbbreviation() != null
                ? unit.getAbbreviation().trim()
                : "";

        String name = unit.getName() != null
                ? unit.getName().toLowerCase()
                : "";

        return "#".equals(abbreviation) || name.contains("unidad");
    }

    /** true si la unidad es el metro (telas). */
    public static boolean isMeterUnit(Unit unit) {

        if (unit == null) {
            return false;
        }

        String abbreviation = unit.getAbbreviation() != null
                ? unit.getAbbreviation().trim().toLowerCase()
                : "";

        String name = unit.getName() != null
                ? unit.getName().toLowerCase()
                : "";

        return "m".equals(abbreviation) || name.contains("metro");
    }
}
