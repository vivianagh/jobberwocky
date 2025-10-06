package io.github.vivianagh.jobberwocky.infrastructure.source;


import io.github.vivianagh.jobberwocky.domain.model.Job;
import io.github.vivianagh.jobberwocky.domain.model.JobSearchCriteria;
import io.github.vivianagh.jobberwocky.exception.ExternalSourceException;
import io.github.vivianagh.jobberwocky.infrastructure.adapter.ExternalJobApiClient;
import io.github.vivianagh.jobberwocky.infrastructure.adapter.ExternalJobResponseAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Now let's put it all together:
 *
 * ExternalJobSource uses the HTTP cliente
 * and adapter to implement JobSource.
 */

@ExtendWith(MockitoExtension.class)
public class ExternalJobSourceTest {

    @Mock
    private ExternalJobApiClient apiClient;

    @Mock
    private ExternalJobResponseAdapter responseAdapter;

    private ExternalJobSource externalJobSource;

    @BeforeEach
    void setUp() {
        externalJobSource = new ExternalJobSource(apiClient, responseAdapter);
    }

    @Test
    void shouldReturnSourceName() {
        // When
        String sourceName = externalJobSource.getSourceName();

        // Then
        assertThat(sourceName).isEqualTo("EXTERNAL_API");
    }

    @Test
    void shouldSearchJobsSuccessfully() {
        // Given
        JobSearchCriteria criteria = JobSearchCriteria.builder()
                .title("Engineer")
                .build();

        String rawJson = "{\"USA\": [[\"Engineer\", 100000, \"<skills></skills>\"]]}";

        Job expectedJob = Job.builder()
                .title("Engineer")
                .country("USA")
                .salary(new BigDecimal("100000"))
                .skills(Set.of())
                .source("EXTERNAL_API")
                .build();

        when(apiClient.fetchJobs(criteria)).thenReturn(rawJson);
        when(responseAdapter.adapt(rawJson)).thenReturn(List.of(expectedJob));

        // When
        List<Job> results = externalJobSource.searchJobs(criteria);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Engineer");
        assertThat(results.get(0).getSource()).isEqualTo("EXTERNAL_API");

        verify(apiClient).fetchJobs(criteria);
        verify(responseAdapter).adapt(rawJson);
    }

    @Test
    void shouldReturnEmptyListWhenApiClientThrowsException() {
        // Given
        JobSearchCriteria criteria = JobSearchCriteria.builder().build();

        when(apiClient.fetchJobs(criteria))
                .thenThrow(new ExternalSourceException("API down"));

        // When
        List<Job> results = externalJobSource.searchJobs(criteria);

        // Then - graceful degradation
        assertThat(results).isEmpty();
        verify(apiClient).fetchJobs(criteria);
        verify(responseAdapter, never()).adapt(anyString());
    }

    @Test
    void shouldReturnEmptyListWhenAdapterThrowsException() {
        // Given
        JobSearchCriteria criteria = JobSearchCriteria.builder().build();
        String rawJson = "invalid json";

        when(apiClient.fetchJobs(criteria)).thenReturn(rawJson);
        when(responseAdapter.adapt(rawJson))
                .thenThrow(new RuntimeException("Parse error"));

        // When
        List<Job> results = externalJobSource.searchJobs(criteria);

        // Then - graceful degradation
        assertThat(results).isEmpty();
    }

    //TODO
}
