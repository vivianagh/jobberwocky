package io.github.vivianagh.jobberwocky.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

public class JobSearchCriteriaTest {

    @Test
    void shouldCreateCriteriaWithAllFields() {
        var c = JobSearchCriteria.builder()
                .title("Engineer")
                .country("USA")
                .minSalary(new BigDecimal(50000))
                .maxSalary(new BigDecimal(100000))
                .skill("Java")
                .build();

        assertThat(c.title()).isEqualTo("Engineer");
        assertThat(c.country()).isEqualTo("USA");
        assertThat(c.minSalary()).isEqualByComparingTo("50000");
        assertThat(c.maxSalary()).isEqualByComparingTo("100000");
        assertThat(c.skill()).isEqualTo("Java");
    }

    @Test
    void shouldCreateEmptyCriteria() {
        var c = JobSearchCriteria.builder().build();
        assertThat(c.title()).isNull();
        assertThat(c.country()).isNull();
        assertThat(c.minSalary()).isNull();
        assertThat(c.maxSalary()).isNull();
        assertThat(c.skill()).isNull();
    }

    @Test
    void validateShouldPassWhenMinLeMax() {
        var c = JobSearchCriteria.builder()
                .minSalary(new BigDecimal("50000"))
                .maxSalary(new BigDecimal("50000"))
                .build();

        assertThatCode(c::validate).doesNotThrowAnyException();
    }

    @Test
    void validateShouldFailWhenMinGtMax() {
        var c = JobSearchCriteria.builder()
                .minSalary(new BigDecimal("100000"))
                .maxSalary(new BigDecimal("50000"))
                .build();

        assertThatThrownBy(c::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("minSalary cannot be greater than maxSalary");
    }
}
