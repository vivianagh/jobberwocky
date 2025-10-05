package io.github.vivianagh.jobberwocky.infrastructure.repository;

import io.github.vivianagh.jobberwocky.domain.model.Job;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;


import org.junit.jupiter.api.Test;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class JobRepositoryTest {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void clean() {
        jobRepository.deleteAll();
    }

    @Test
    void shouldSaveJob() {
        Job  job = Job.builder()
                .title("Backend Developer")
                .description("Build APIs")
                .company("Tech Corp")
                .country("USA")
                .city("New York")
                .salary(new BigDecimal("120000"))
                .skills(Set.of("Java", "Spring Boot"))
                .source("INTERNAL")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Job saved = jobRepository.save(job);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("Backend Developer");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getSkills()).containsExactlyInAnyOrder("Java", "Spring Boot");
    }

    @Test
    void shouldFindJobById() {
        Job job = Job.builder()
                    .title("Frontend Developer")
                    .company("Web Co")
                    .country("Spain")
                    .salary(new BigDecimal("60000"))
                    .skills(Set.of("React"))
                    .source("INTERNAL")
                    .build();

        Job persisted = entityManager.persistFlushFind(job);

        Optional<Job> found = jobRepository.findById(persisted.getId());

        assertThat(found.isPresent()).isTrue();
        assertThat(found.get().getTitle()).isEqualTo("Frontend Developer");
        assertThat(found.get().getSkills()).containsExactly("React");

    }

    @Test
    void shouldFindAllJobs() {
        //Given
        Job a = Job.builder()
                .title("Developer 1")
                .company("Company A")
                .country("USA")
                .salary(new BigDecimal("100000"))
                .skills(Set.of("Java"))
                .source("INTERNAL")
                .build();
        Job b = Job.builder()
                .title("Developer 2")
                .company("Company B")
                .country("Spain")
                .salary(new BigDecimal("80000"))
                .skills(Set.of("Python"))
                .source("INTERNAL")
                .build();

        entityManager.persist(a);
        entityManager.persist(b);
        entityManager.flush();
        //When
        List<Job> all = jobRepository.findAll();

        //Then
        assertThat(all).hasSize(2);
        assertThat(all).extracting(Job::getTitle)
                .containsExactlyInAnyOrder("Developer 1", "Developer 2");
    }

    @Test
    void shouldDeleteJob() {
        //Given
        Job job = Job.builder()
                .title("Temporary Job")
                .company("Temp Co")
                .country("UK")
                .salary(new BigDecimal("50000"))
                .skills(Set.of("Testing"))
                .source("INTERNAL")
                .build();

        Job saved = entityManager.persistFlushFind(job);
        Long jobId = saved.getId();

        //When
        jobRepository.deleteById(jobId);
        entityManager.flush();

        // Then
        Optional<Job> found = jobRepository.findById(jobId);
        assertThat(found).isEmpty();
    }

    @Test
    void shouldUpdateJob() {
        //Given
        Job job = Job.builder()
                .title("Original Title")
                .company("Company")
                .country("USA")
                .salary(new BigDecimal("100000"))
                .skills(Set.of("Skill1"))
                .build();

        Job saved = entityManager.persistFlushFind(job);
        Long jobId = saved.getId();

        //When
        saved.setTitle("Updated Title");
        saved.setSalary(new BigDecimal("150000"));
        Job updated = jobRepository.save(saved);
        entityManager.flush();

        //Then
        Job found = jobRepository.findById(jobId).orElseThrow();
        assertThat(found.getTitle()).isEqualTo("Updated Title");
        assertThat(found.getSalary()).isEqualByComparingTo(new BigDecimal("150000"));
        assertThat(found.getUpdatedAt()).isAfter(found.getCreatedAt());

    }

    @Test
    void shouldPersistSkillsCorrectly() {
        // Given
        Job job = Job.builder()
                .title("Full Stack Developer")
                .company("Startup")
                .country("Germany")
                .salary(new BigDecimal("90000"))
                .skills(Set.of("Java", "React", "Docker", "Kubernetes"))
                .build();

        // When
        Job saved = jobRepository.save(job);
        entityManager.flush();
        entityManager.clear(); // Clear persistence context to force reload

        // Then
        Job found = jobRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getSkills()).hasSize(4);
        assertThat(found.getSkills()).containsExactlyInAnyOrder(
                "Java", "React", "Docker", "Kubernetes"
        );
    }

}
