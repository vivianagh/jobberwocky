package io.github.vivianagh.jobberwocky.infrastructure.source;

import io.github.vivianagh.jobberwocky.domain.model.Job;
import io.github.vivianagh.jobberwocky.domain.model.JobSearchCriteria;
import io.github.vivianagh.jobberwocky.domain.port.JobSource;
import io.github.vivianagh.jobberwocky.infrastructure.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "sources.internal", name = "enabled", havingValue = "true", matchIfMissing = true)
public class InternalJobSource implements JobSource {

    private final JobRepository jobRepository;

    @Override
    public List<Job> searchJobs(JobSearchCriteria criteria) {
        // Validate input
        if (criteria == null) {
            log.warn("Search criteria is null, returning empty list");
            return List.of();
        }

        log.debug("Searching internal jobs with criteria: {}", criteria);

        Specification<Job> spec = buildSpecification(criteria);
        List<Job> jobs = jobRepository.findAll(spec);

        log.debug("Found {} jobs from internal source", jobs.size());
        return jobs;
    }

    @Override
    public String getSourceName() {
        return "INTERNAL";
    }

    /**
     * Specifications allow dynamic query building
     * Each criterion adds a WHERE clause
     */
    private Specification<Job> buildSpecification(JobSearchCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by title (case-insensitive LIKE)
            if (criteria.title() != null && !criteria.title().isBlank()) {
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("title")),
                                "%" + criteria.title().toLowerCase() + "%"
                        )
                );
            }

            // Filter by country (case-insensitive exact match)
            if (criteria.country() != null && !criteria.country().isBlank()) {
                predicates.add(
                        criteriaBuilder.equal(
                                criteriaBuilder.lower(root.get("country")),
                                criteria.country().toLowerCase()
                        )
                );
            }

            // Filter by minimum salary
            if (criteria.minSalary() != null) {
                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(
                                root.get("salary"),
                                criteria.minSalary()
                        )
                );
            }

            // Filter by maximum salary
            if (criteria.maxSalary() != null) {
                predicates.add(
                        criteriaBuilder.lessThanOrEqualTo(
                                root.get("salary"),
                                criteria.maxSalary()
                        )
                );
            }

            // Combine all predicates with AND
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
