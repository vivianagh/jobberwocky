package io.github.vivianagh.jobberwocky.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class JobTest {

    @Test
    void shouldCreateJobWithAllFields() {
       //Given and when
        Job job = Job.builder()
                .id(1L)
                .title("Senior Java Developer")
                .description("Build amazing systems")
                .company("Tech Corp")
                .country("USA")
                .city("San Francisco")
                .salary(new BigDecimal(1500000))
                .skills(Set.of("Java" , "SpringBoot", "Microservices"))
                .source("INTERNAL")
                .externalId("EXT_123")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        assertThat(job.getId()).isEqualTo(1L);
        assertThat(job.getTitle()).isEqualTo("Senior Java Developer");
        assertThat(job.getDescription()).isEqualTo("Build amazing systems");
        assertThat(job.getCompany()).isEqualTo("Tech Corp");
        assertThat(job.getCountry()).isEqualTo("USA");
        assertThat(job.getCity()).isEqualTo("San Francisco");
        assertThat(job.getSalary()).isEqualByComparingTo(new BigDecimal("1500000"));
        assertThat(job.getSkills()).containsExactlyInAnyOrder("Java" , "SpringBoot", "Microservices");
        assertThat(job.getSource()).isEqualTo("INTERNAL");
        assertThat(job.getExternalId()).isEqualTo("EXT_123");
        assertThat(job.getCreatedAt()).isNotNull();
        assertThat(job.getUpdatedAt()).isNotNull();
    }


}
