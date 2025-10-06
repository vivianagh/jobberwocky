package io.github.vivianagh.jobberwocky.application.service;

import io.github.vivianagh.jobberwocky.application.servicio.JobService;
import io.github.vivianagh.jobberwocky.domain.model.Job;
import io.github.vivianagh.jobberwocky.domain.model.JobSearchCriteria;
import io.github.vivianagh.jobberwocky.domain.port.JobSource;
import io.github.vivianagh.jobberwocky.infrastructure.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class JobServiceTest {

    private JobRepository jobRepository;
    private JobSource jobSource;
    private JobService jobService;

    @BeforeEach
    void setUp() {
        jobRepository = mock(JobRepository.class);
        jobSource = mock(JobSource.class);
        jobService = new JobService(jobRepository, jobSource);
    }

    @Test
    void createSetsInternalSource() {
        //Given
        Job in = Job.builder()
                .title("Backend Eng")
                .company("Acme")
                .country("Colombia")
                .salary(new BigDecimal(120000))
                .skills(Set.of("Java"))
                .build();

        Job saved = Job.builder().id(1L).title("Backend Eng")
                .company("Acme")
                .country("Colombia")
                .salary(new BigDecimal(120000))
                .skills(Set.of("Java"))
                .source("INTERNAL")
                .build();

        when(jobRepository.save(any(Job.class))).thenReturn(saved);
        //When
        Job out = jobService.create(in);

        //Then
        assertThat(out.getId()).isEqualTo(1L);
        assertThat(out.getSource()).isEqualTo("INTERNAL");
        verify(jobRepository).save(any(Job.class));
        verifyNoInteractions(jobSource);
    }

    @Test
    void searchDelegatesToPrimaryJobSource() {
        //Given
        JobSearchCriteria criteria = JobSearchCriteria.builder().title("Eng").build();
        when(jobSource.searchJobs(any())).thenReturn(List.of());

        //When
        var results = jobService.search(criteria);

        //Then
        assertThat(results).isEmpty();
        verify(jobSource).searchJobs(criteria);
    }
}
