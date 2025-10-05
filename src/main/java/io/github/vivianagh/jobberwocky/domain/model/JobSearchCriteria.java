package io.github.vivianagh.jobberwocky.domain.model;


import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record JobSearchCriteria(
        String title,
        String country,
        BigDecimal minSalary,
        BigDecimal maxSalary,
        String skill
) {
    public void validate() {
        if (minSalary != null && maxSalary != null && minSalary.compareTo(maxSalary) > 0) {
            throw new IllegalArgumentException("minSalary cannot be greater than maxSalary");
        }
    }

}
