package com.twelveaxes.service;

import com.twelveaxes.model.AxisResult;
import com.twelveaxes.model.Country;
import com.twelveaxes.model.CountryMatch;
import com.twelveaxes.model.PersonalityMatch;
import com.twelveaxes.model.TimelineEntry;
import com.twelveaxes.model.TimelineEra;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Monta a linha do tempo: em cada uma das seis eras, os perfis mais compativeis entre
 * personalidades (era da morte) e paises (atuais no seculo XXI, historicos pelo primeiro ano do
 * periodo), misturados num so ranking.
 */
@Service
public class TimelineService {
    /** Perfis por era. */
    static final int PER_ERA = 4;

    /** Ano que coloca os paises atuais na ultima era. */
    private static final int CURRENT_YEAR = 2000;

    private final QuizDataService dataService;
    private final PersonalityMatcherService personalityMatcherService;
    private final CountryMatcherService countryMatcherService;

    public TimelineService(
            QuizDataService dataService,
            PersonalityMatcherService personalityMatcherService,
            CountryMatcherService countryMatcherService
    ) {
        this.dataService = dataService;
        this.personalityMatcherService = personalityMatcherService;
        this.countryMatcherService = countryMatcherService;
    }

    public List<TimelineEra> build(List<AxisResult> axisResults, String lang, String religion) {
        Map<String, Country> countries = dataService.getCountries(lang).stream()
                .collect(Collectors.toMap(Country::id, Function.identity()));

        List<TimelineCandidate> candidates = new ArrayList<>();
        for (PersonalityMatch match : personalityMatcherService.findAll(axisResults, lang, religion)) {
            candidates.add(new TimelineCandidate(
                    EraResolver.yearOfDeath(match.lifespan()),
                    new TimelineEntry("personality", match, null)));
        }
        for (CountryMatch match : countryMatcherService.findAll(axisResults, lang, religion)) {
            candidates.add(new TimelineCandidate(
                    foundingYear(countries.get(match.countryId())),
                    new TimelineEntry("country", null, match)));
        }

        Comparator<TimelineEntry> byCompatibility =
                Comparator.comparingDouble(TimelineEntry::compatibility).reversed().thenComparing(TimelineEntry::name);

        return EraResolver.ERAS.stream()
                .map(era -> new TimelineEra(
                        era.key(),
                        era.fromYear() == Integer.MIN_VALUE ? null : era.fromYear(),
                        era.toYear(),
                        candidates.stream()
                                .filter(candidate -> EraResolver.eraOfYear(candidate.year()) == era)
                                .map(TimelineCandidate::entry)
                                .sorted(byCompatibility)
                                .limit(PER_ERA)
                                .toList()))
                .toList();
    }

    // Pais atual (historical false) e do seculo XXI; historico entra pelo primeiro ano do period.
    // Sem ano legivel (nao ha caso no catalogo, o teste garante) o perfil cai na era aberta.
    private static int foundingYear(Country country) {
        if (!country.historical()) {
            return CURRENT_YEAR;
        }
        return EraResolver.startYear(country.period()).orElse(CURRENT_YEAR);
    }

    private record TimelineCandidate(int year, TimelineEntry entry) {
    }
}
