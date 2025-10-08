package io.github.vivianagh.jobberwocky.infrastructure.source;

import io.github.vivianagh.jobberwocky.domain.model.Job;
import io.github.vivianagh.jobberwocky.domain.model.JobSearchCriteria;
import io.github.vivianagh.jobberwocky.domain.port.JobSource;
import io.github.vivianagh.jobberwocky.infrastructure.adapter.ExternalJobApiClient;
import io.github.vivianagh.jobberwocky.infrastructure.adapter.ExternalJobResponseAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "sources.external", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ExternalJobSource implements JobSource {

    private final ExternalJobApiClient apiClient;
    private final ExternalJobResponseAdapter responseAdapter;


    @Override
    public List<Job> searchJobs(JobSearchCriteria criteria) {
        log.debug("Searching external jobs with criteria: {}", criteria);
        try {
            // Step 1: Call external API
            String rawResponse = apiClient.fetchJobs(criteria);

            // Step 2: Transform messy format to our domain model
            List<Job> jobs = responseAdapter.adapt(rawResponse);

            // Step 3: Mark all jobs with external source
            jobs.forEach(job -> job.setSource(getSourceName()));

            log.debug("Found {} jobs from external source", jobs.size());
            return jobs;
        } catch (Exception e) {
            log.error("Error fetching from external source: {}", e.getMessage(), e);
            // Graceful degradation - don't fail entire search
            // Return empty list and let other sources provide results
            return List.of();
        }

    }

    @Override
    public String getSourceName() {
        return "EXTERNAL_API";
    }

    @Override
    public boolean isAvailable() {
        try {
            return apiClient.healthCheck();
        } catch (Exception e) {
            log.warn("External source health check failed", e);
            return false;
        }
    }
}
