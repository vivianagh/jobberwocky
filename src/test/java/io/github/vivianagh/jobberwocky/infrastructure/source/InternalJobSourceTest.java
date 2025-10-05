package io.github.vivianagh.jobberwocky.infrastructure.source;

import io.github.vivianagh.jobberwocky.domain.model.Job;
import io.github.vivianagh.jobberwocky.domain.model.JobSearchCriteria;
import io.github.vivianagh.jobberwocky.infrastructure.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class InternalJobSourceTest {

    @Mock
    private JobRepository jobRepository;

    private InternalJobSource internalJobSource;

    @BeforeEach
    void setUp() {
        internalJobSource = new InternalJobSource(jobRepository);
    }

    @Test
    void shouldReturnSourceName() {
        //When
        String sourceName = internalJobSource.getSourceName();

        //Then
        assertThat(sourceName).isEqualTo("INTERNAL");
    }

    @Test
    void shouldBeAvailableByDefault() {
        // When
        boolean available = internalJobSource.isAvailable();

        // Then
        assertThat(available).isTrue();
    }

    @Test
    void shouldSearchJobWithEmptyCriteria() {
        // Given
        JobSearchCriteria criteria = JobSearchCriteria.builder().build();

        Job job1 = createJob("Developer 1", "USA", new BigDecimal("100000"));
        Job job2 = createJob("Developer 2", "Spain", new BigDecimal("80000"));

        when(jobRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(job1, job2));

        // When
        List<Job> results = internalJobSource.searchJobs(criteria);

        // Then
        assertThat(results).hasSize(2);
        verify(jobRepository).findAll(any(Specification.class));

    }

    @Test
    void shouldSearchJobsByTitle() {
        // Given
        JobSearchCriteria criteria = JobSearchCriteria.builder()
                .title("Engineer")
                .build();

        Job matchingJob = createJob("Software Engineer", "USA", new BigDecimal("120000"));

        when(jobRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(matchingJob));

        // When
        List<Job> results = internalJobSource.searchJobs(criteria);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).contains("Engineer");
        verify(jobRepository).findAll(any(Specification.class));
    }

    // Helper method
    private Job createJob(String title, String country, BigDecimal salary) {
        return Job.builder()
                .title(title)
                .company("Test Company")
                .country(country)
                .salary(salary)
                .skills(Set.of("Java"))
                .source("INTERNAL")
                .build();
    }
}
