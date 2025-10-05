package io.github.vivianagh.jobberwocky.domain.port;

import io.github.vivianagh.jobberwocky.domain.model.Job;
import io.github.vivianagh.jobberwocky.domain.model.JobSearchCriteria;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class JobSourceTest {

    @Test
    void shouldHaveDefaultIsAvailableReturningTrue() {
        // Given
        JobSource source = new TestJobSource();

        // When
        boolean available = source.isAvailable();

        // Then
        assertThat(available).isTrue();
    }

    @Test
    void shouldAllowOverridingIsAvailable() {
        // Given
        JobSource source = new UnavailableJobSource();

        // When
        boolean available = source.isAvailable();

        // Then
        assertThat(available).isFalse();
    }

    // Test implementations
    private static class TestJobSource implements JobSource {
        @Override
        public List<Job> searchJobs(JobSearchCriteria criteria) {
            return List.of(
                    Job.builder()
                            .title("Test Job")
                            .company("Test Company")
                            .country("USA")
                            .salary(new BigDecimal("100000"))
                            .skills(Set.of("Java"))
                            .build()
            );
        }

        @Override
        public String getSourceName() {
            return "TEST";
        }
    }

    private static class UnavailableJobSource implements JobSource {
        @Override
        public List<Job> searchJobs(JobSearchCriteria criteria) {
            return List.of();
        }

        @Override
        public String getSourceName() {
            return "UNAVAILABLE";
        }

        @Override
        public boolean isAvailable() {
            return false;
        }
    }
}
