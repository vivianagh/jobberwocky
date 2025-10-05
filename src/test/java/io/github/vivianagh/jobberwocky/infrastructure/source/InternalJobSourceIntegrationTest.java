package io.github.vivianagh.jobberwocky.infrastructure.source;

import io.github.vivianagh.jobberwocky.domain.model.Job;
import io.github.vivianagh.jobberwocky.domain.model.JobSearchCriteria;
import io.github.vivianagh.jobberwocky.infrastructure.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@Import(InternalJobSource.class)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class InternalJobSourceIntegrationTest {

    @Autowired
    private InternalJobSource internalJobSource;

    @Autowired
    private JobRepository jobRepository;

    @BeforeEach
    void setUp() {
        jobRepository.deleteAll();

        // Create test data
        createTestJobs();
    }

    @Test
    void shouldSearchAllJobsWhenNoCriteriaProvided() {
        //Given
        JobSearchCriteria criteria = JobSearchCriteria.builder().build();

        //When
        List<Job> jobs = internalJobSource.searchJobs(criteria);

        //Then
        assertThat(jobs).hasSize(5);
    }

    @Test
    void shouldSearchJobsByTitle() {
        //Given
        JobSearchCriteria criteria = JobSearchCriteria.builder()
                .title("Engineer")
                .build();

        //When
        List<Job> jobs = internalJobSource.searchJobs(criteria);

        //Then
        assertThat(jobs).hasSize(3);
        assertThat(jobs).allMatch(job -> job.getTitle().toLowerCase().contains("engineer"));
    }

    @Test
    void shouldSearchJobsByCountry() {
        // Given
        JobSearchCriteria criteria = JobSearchCriteria.builder()
                .country("USA")
                .build();

        // When
        List<Job> results = internalJobSource.searchJobs(criteria);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).allMatch(job -> job.getCountry().equals("USA"));
    }

    @Test
    void shouldSearchJobsByMinSalary() {
        // Given
        JobSearchCriteria criteria = JobSearchCriteria.builder()
                .minSalary(new BigDecimal("100000"))
                .build();

        // When
        List<Job> results = internalJobSource.searchJobs(criteria);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results).allMatch(job ->
                job.getSalary().compareTo(new BigDecimal("100000")) >= 0
        );
    }

    private void createTestJobs() {
        List<Job> jobs = List.of(
                Job.builder()
                        .title("Senior Backend Engineer")
                        .description("Build scalable systems")
                        .company("Tech Corp")
                        .country("USA")
                        .city("San Francisco")
                        .salary(new BigDecimal("150000"))
                        .skills(Set.of("Java", "Spring Boot", "Microservices"))
                        .source("INTERNAL")
                        .build(),

                Job.builder()
                        .title("Frontend Engineer")
                        .description("Create beautiful UIs")
                        .company("Web Co")
                        .country("Spain")
                        .city("Barcelona")
                        .salary(new BigDecimal("80000"))
                        .skills(Set.of("React", "TypeScript", "CSS"))
                        .source("INTERNAL")
                        .build(),
                Job.builder()
                        .title("DevOps Engineer")
                        .description("Manage infrastructure")
                        .company("Cloud Systems")
                        .country("Germany")
                        .city("Berlin")
                        .salary(new BigDecimal("90000"))
                        .skills(Set.of("Docker", "Kubernetes", "AWS"))
                        .source("INTERNAL")
                        .build(),

                Job.builder()
                        .title("Junior Developer")
                        .description("Learn and grow")
                        .company("StartUp Inc")
                        .country("USA")
                        .city("Austin")
                        .salary(new BigDecimal("60000"))
                        .skills(Set.of("Python", "Django"))
                        .source("INTERNAL")
                        .build(),

                Job.builder()
                        .title("Data Scientist")
                        .description("Analyze data")
                        .company("Data Corp")
                        .country("UK")
                        .city("London")
                        .salary(new BigDecimal("95000"))
                        .skills(Set.of("Python", "TensorFlow", "SQL"))
                        .source("INTERNAL")
                        .build()
        );
        jobRepository.saveAll(jobs);
    }
}
