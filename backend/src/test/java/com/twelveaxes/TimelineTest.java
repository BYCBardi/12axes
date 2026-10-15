package com.twelveaxes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twelveaxes.model.Country;
import com.twelveaxes.model.QuizResult;
import com.twelveaxes.model.TimelineEntry;
import com.twelveaxes.model.TimelineEra;
import com.twelveaxes.service.EraResolver;
import com.twelveaxes.service.QuizDataService;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class TimelineTest {
    private static final String VECTOR = "70,80,20,30,70,40,60,55,60,25,20,45";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private QuizDataService dataService;

    private QuizResult result(String religion) throws Exception {
        var request = get("/api/results/by-axes").param("v", VECTOR);
        if (religion != null) {
            request = request.param("religion", religion);
        }
        String body = mockMvc.perform(request).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readValue(body, QuizResult.class);
    }

    @Test
    void everyEraHasFourProfilesSortedByCompatibility() throws Exception {
        for (String religion : new String[] {null, "catholic", "islam", "judaism"}) {
            List<TimelineEra> timeline = result(religion).timeline();

            assertThat(timeline).extracting(TimelineEra::key)
                    .containsExactly("antiguidade", "medieval", "moderna", "xix", "xx", "xxi");
            for (TimelineEra era : timeline) {
                assertThat(era.entries()).as("%s com religiao %s", era.key(), religion).hasSize(4);
                assertThat(era.entries())
                        .isSortedAccordingTo((a, b) -> Double.compare(b.compatibility(), a.compatibility()));
                assertThat(era.entries()).allSatisfy(entry ->
                        assertThat(entry.type().equals("personality") ? entry.personality() : entry.country())
                                .isNotNull());
            }
        }
    }

    @Test
    void profilesLandInTheEraOfTheirDeathOrFoundation() throws Exception {
        for (TimelineEra era : result(null).timeline()) {
            for (TimelineEntry entry : era.entries()) {
                int year = entry.personality() != null
                        ? EraResolver.yearOfDeath(entry.personality().lifespan())
                        : foundingYear(dataService.getCountryById(entry.country().countryId()));
                assertThat(EraResolver.eraOfYear(year).key()).as(entry.name()).isEqualTo(era.key());
            }
        }
    }

    @Test
    void everyHistoricalCountryHasAReadableStartYear() {
        for (Country country : dataService.getCountries()) {
            if (country.historical()) {
                assertThat(EraResolver.startYear(country.period()))
                        .as("periodo de %s", country.id()).isPresent();
            }
        }
    }

    // Pais atual = seculo XXI; historico = primeiro ano do periodo.
    private static int foundingYear(Country country) {
        return country.historical() ? EraResolver.startYear(country.period()).orElseThrow() : 2000;
    }
}
