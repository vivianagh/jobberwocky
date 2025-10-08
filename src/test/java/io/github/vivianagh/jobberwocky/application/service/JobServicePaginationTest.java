package io.github.vivianagh.jobberwocky.application.service;



import io.github.vivianagh.jobberwocky.application.servicio.JobService;
import io.github.vivianagh.jobberwocky.domain.model.Job;
import io.github.vivianagh.jobberwocky.domain.model.JobSearchCriteria;
import io.github.vivianagh.jobberwocky.domain.port.JobSource;
import io.github.vivianagh.jobberwocky.infrastructure.repository.JobRepository;
import io.github.vivianagh.jobberwocky.infrastructure.web.dto.PageResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class JobServicePaginationTest {

    JobRepository jobRepository;
    JobSource jobSource;
    JobService service;

    @BeforeEach
    void setUp() {
        jobRepository = Mockito.mock(JobRepository.class);
        jobSource = Mockito.mock(JobSource.class);
        service = new JobService(jobRepository, jobSource);
    }

    private Job j(long id, String title, String country, int salary) {
        return Job.builder()
                .id(id)
                .title(title)
                .company("C")
                .country(country)
                .salary(new BigDecimal(salary))
                .skills(Set.of("X"))
                .source("INTERNAL")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void shoudSearchPagedOrderBySalaryDesc() throws Exception {
        // given: 3 jobs
        var all = List.of(
                j(1, "A", "USA", 100_000),
                j(2, "B", "USA", 120_000),
                j(3, "C", "USA", 90_000)
        );
        when(jobSource.searchJobs(any(JobSearchCriteria.class))).thenReturn(all);

        // when: page=0 size=2 sort=salary,desc
        PageResponse<Job> page = service.searchPaged(
                JobSearchCriteria.builder().build(),
                0, 2, "salary,desc"
        );

        // then
        assertThat(page.page()).isEqualTo(0);
        assertThat(page.size()).isEqualTo(2);
        assertThat(page.total()).isEqualTo(3);
        assertThat(page.content()).hasSize(2);
        assertThat(page.content().get(0).getSalary())
                .isGreaterThanOrEqualTo(page.content().get(1).getSalary());
        // top-2 should be 120k and 100k
        assertThat(page.content()).extracting(jb -> jb.getSalary().intValue())
                .containsExactly(120_000, 100_000);
    }


}
