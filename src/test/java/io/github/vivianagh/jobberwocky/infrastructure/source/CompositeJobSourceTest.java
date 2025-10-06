package io.github.vivianagh.jobberwocky.infrastructure.source;


import io.github.vivianagh.jobberwocky.domain.model.Job;
import io.github.vivianagh.jobberwocky.domain.model.JobSearchCriteria;
import io.github.vivianagh.jobberwocky.domain.port.JobSource;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

public class CompositeJobSourceTest {
    private static Job job(String title,
                           String country,
                           int salary,
                           String source,
                           String externalId,
                           Long id) {
        return Job.builder()
                .id(id)
                .title(title)
                .company("Company")
                .country(country)
                .salary(new BigDecimal(salary))
                .skills(Set.of("Skill"))
                .source(source)
                .externalId(externalId)
                .build();
    }

    private static JobSource mockSource(String name, boolean available, List<Job> results) {
        JobSource s = mock(JobSource.class);
        when(s.getSourceName()).thenReturn(name);
        when(s.isAvailable()).thenReturn(available);
        when(s.searchJobs(any())).thenReturn(results);
        return s;
    }

    private static JobSource failingSource(String name) {
        JobSource s = mock(JobSource.class);
        when(s.getSourceName()).thenReturn(name);
        when(s.isAvailable()).thenReturn(true);
        when(s.searchJobs(any())).thenThrow(new RuntimeException("boom"));
        return s;
    }

    @Test
    void aggregatesResultsFromMultipleSources() {
        JobSource s1 = mockSource("S1", true,
                List.of(job("Backend Eng", "USA", 120000, "INTERNAL", null, 11L)));
        JobSource s2 = mockSource("S2", true,
                List.of(job("Data Eng", "Spain", 90000, "EXTERNAL_API", "EXT_ES_DATA_90K", null)));

        CompositeJobSource composite = new CompositeJobSource(List.of(s1, s2));

        java.util.List<Job> out = composite.searchJobs(JobSearchCriteria.builder().build());

        assertThat(out).hasSize(2);
        assertThat(out).extracting(Job::getTitle)
                .containsExactlyInAnyOrder("Backend Eng", "Data Eng");

        verify(s1, times(1)).searchJobs(any());
        verify(s2, times(1)).searchJobs(any());
    }

    @Test
    void shouldToleratedFailingSourceWithoutBreakingTheSearch() {
        //Given
        JobSource unavailable = mockSource(
                "DOWN", false,
                List.of(job("X", "Y", 1, "INTERNAL", null, 1L))
        );
        JobSource fails = failingSource("FAILS");

        JobSource ok = mockSource("OK", true,
                List.of(job("Platform Eng", "USA", 130000, "INTERNAL", null, 42L))
        );
        CompositeJobSource composite = new CompositeJobSource(List.of(unavailable, fails, ok));
        //When
        var out = composite.searchJobs(JobSearchCriteria.builder().build());

        //Then
        //Only ok result
        assertThat(out).hasSize(1);
        assertThat(out.get(0).getTitle()).isEqualTo("Platform Eng");
        verify(fails, times(1)).searchJobs(any());
        verify(ok, times(1)).searchJobs(any());
    }

    @Test
    void shouldDeduplicatesByExternalIdAndPrefersInternal() {
        //Given
        //Same externalId
        Job externalDup = job(
                "DevOps Eng", "USA", 110000,"EXTERNAL_API",
                "EXT_US_DEVOPS_110", null
        );
        Job internalDup = job("DevOps Eng", "USA", 110000, "INTERNAL",
                "EXT_US_DEVOPS_110", 99L
        );

        JobSource sExt = mockSource("EXT", true, List.of(externalDup));
        JobSource sInt = mockSource("INT", true, List.of(internalDup));

        CompositeJobSource composite = new CompositeJobSource(List.of(sExt, sInt));

        var out = composite.searchJobs(JobSearchCriteria.builder().build());

        assertThat(out).hasSize(1);
        assertThat(out.get(0).getSource()).isEqualTo("INTERNAL");
        assertThat(out.get(0).getExternalId()).isEqualTo("EXT_US_DEVOPS_110");
    }

}
