package io.github.vivianagh.jobberwocky.domain.port;


import io.github.vivianagh.jobberwocky.domain.model.Job;
import io.github.vivianagh.jobberwocky.domain.model.JobSearchCriteria;

import java.util.List;

/**
 * Port interface for job sources (Hexagonal Architecture)
 *
 * This is the KEY to extensibility and demonstrates:
 * - Open/Closed Principle (open for extension, closed for modification)
 * - Dependency Inversion Principle (depend on abstraction, not concrete)
 * - Strategy Pattern (different sources, same interface)
 *
 * Each implementation represents a different job source:
 * - InternalJobSource: Our database
 * - ExternalJobSource: External API
 * - LinkedInJobSource: LinkedIn (future)
 * - etc.
 */

public interface JobSource {
    List<Job> searchJobs(JobSearchCriteria criteria);

    String getSourceName();

    default boolean isAvailable() {
        return true;
    }
}
