package io.github.vivianagh.jobberwocky.infrastructure.source;

import io.github.vivianagh.jobberwocky.domain.model.Job;
import io.github.vivianagh.jobberwocky.domain.model.JobSearchCriteria;
import io.github.vivianagh.jobberwocky.domain.port.JobSource;
import io.github.vivianagh.jobberwocky.infrastructure.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * This is for verifies the contract between interface and implementation
 */
@DataJpaTest
@Import(InternalJobSource.class)
public class InternalJobSourceContractTest {
    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private InternalJobSource internalJobSource;

    // Reference as interface - demonstrates LSP
    private JobSource jobSource;

    @BeforeEach
    void setUp() {
        jobRepository.deleteAll();

        // Assign concrete implementation to interface reference
        // This is key for LSP - we use the abstraction
        jobSource = internalJobSource;
    }

    @Test
    void shouldImplementJobSourceInterface() {
        // Then
        assertThat(jobSource).isInstanceOf(JobSource.class);
        assertThat(jobSource).isInstanceOf(InternalJobSource.class);
    }

    @Test
    void shouldReturnEmptyListWhenSearchingWithNullCriteria() {
        // Given
        JobSearchCriteria criteria = null;

        // When
        List<Job> results = jobSource.searchJobs(criteria);

        // Then
        assertThat(results).isNotNull();
        assertThat(results).isEmpty();
    }

    @Test
    void shouldReturnListOfJobsFromSearchJobs() {
        // Given
        createTestJob();
        JobSearchCriteria criteria = JobSearchCriteria.builder().build();

        // When
        List<Job> results = jobSource.searchJobs(criteria);

        // Then
        assertThat(results).isNotNull();
        assertThat(results).isInstanceOf(List.class);
    }

    private void createTestJob() {
        Job job = Job.builder()
                .title("Test Developer")
                .company("Test Co")
                .country("USA")
                .salary(new BigDecimal("100000"))
                .skills(Set.of("Java"))
                .source("INTERNAL")
                .build();

        jobRepository.save(job);
    }
}
