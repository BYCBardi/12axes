package com.twelveaxes.model;

/**
 * Um perfil da linha do tempo: uma personalidade OU um pais/regime (o outro campo e nulo).
 *
 * @param type "personality" ou "country"
 * @param personality a personalidade, quando type for "personality"
 * @param country o pais ou regime, quando type for "country"
 */
public record TimelineEntry(String type, PersonalityMatch personality, CountryMatch country) {
    public double compatibility() {
        return personality != null ? personality.compatibility() : country.compatibility();
    }

    public String name() {
        return personality != null ? personality.name() : country.name();
    }
}
