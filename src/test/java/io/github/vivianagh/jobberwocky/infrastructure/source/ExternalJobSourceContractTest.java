package io.github.vivianagh.jobberwocky.infrastructure.source;


import io.github.vivianagh.jobberwocky.domain.model.Job;
import io.github.vivianagh.jobberwocky.domain.model.JobSearchCriteria;
import io.github.vivianagh.jobberwocky.domain.port.JobSource;
import io.github.vivianagh.jobberwocky.infrastructure.adapter.ExternalJobApiClient;
import io.github.vivianagh.jobberwocky.infrastructure.adapter.ExternalJobResponseAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExternalJobSourceContractTest {

    @Mock
    private ExternalJobApiClient apiClient;

    @Mock
    private ExternalJobResponseAdapter responseAdapter;

    // Reference as interface - demonstrates LSP
    private JobSource jobSource;

    @BeforeEach
    void setUp() {
        ExternalJobSource externalJobSource = new ExternalJobSource(apiClient, responseAdapter);
        // Assign concrete implementation to interface reference
        jobSource = externalJobSource;
    }

    @Test
    void shouldImplementJobSourceInterface() {
        // Then
        assertThat(jobSource).isInstanceOf(JobSource.class);
        assertThat(jobSource).isInstanceOf(ExternalJobSource.class);
    }

    @Test
    void shouldReturnListOfJobsFromSearchJobs() {
        // Given
        when(apiClient.fetchJobs(any())).thenReturn("{}");
        when(responseAdapter.adapt(any())).thenReturn(List.of());

        JobSearchCriteria criteria = JobSearchCriteria.builder().build();

        // When
        List<Job> results = jobSource.searchJobs(criteria);

        // Then
        assertThat(results).isNotNull();
        assertThat(results).isInstanceOf(List.class);
    }
}
