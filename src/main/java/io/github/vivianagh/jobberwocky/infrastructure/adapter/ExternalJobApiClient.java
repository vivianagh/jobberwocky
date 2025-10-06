package io.github.vivianagh.jobberwocky.infrastructure.adapter;

import io.github.vivianagh.jobberwocky.domain.model.JobSearchCriteria;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import io.github.vivianagh.jobberwocky.exception.ExternalSourceException;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExternalJobApiClient {

    private final RestClient restClient;

    /**
     * Fetch jobs from external API
     *
     * Calls: http://localhost:8081/jobs?name=X&country=Y&salary_min=Z&salary_max=W
     *
     * @param criteria search filters
     * @return JSON response as String
     * @throws ExternalSourceException if HTTP call fails
     */
    public String fetchJobs(JobSearchCriteria criteria) {
        try {
            log.debug("Calling external API with criteria: {}", criteria);

            String response = restClient.get()
                    .uri(uriBuilder -> {
                        // Build URI with query parameters
                        uriBuilder.path("/jobs");

                        // Add title as 'name' parameter (external API uses 'name')
                        if (criteria.title() != null && !criteria.title().isBlank()) {
                            uriBuilder.queryParam("name", criteria.title());
                        }

                        // Add country parameter
                        if (criteria.country() != null && !criteria.country().isBlank()) {
                            uriBuilder.queryParam("country", criteria.country());
                        }

                        // Add min salary parameter
                        if (criteria.minSalary() != null) {
                            uriBuilder.queryParam("salary_min", criteria.minSalary());
                        }

                        // Add max salary parameter
                        if (criteria.maxSalary() != null) {
                            uriBuilder.queryParam("salary_max", criteria.maxSalary());
                        }

                        return uriBuilder.build();
                    })
                    .retrieve()
                    .body(String.class);

            log.debug("External API response received (length: {} chars)",
                    response != null ? response.length() : 0);

            return response;

        } catch (RestClientException e) {
            log.error("External API call failed: {}", e.getMessage(), e);
            throw new ExternalSourceException("Failed to fetch external jobs", e);
        } catch (Exception e) {
            log.error("Unexpected error calling external API", e);
            throw new ExternalSourceException("Unexpected error fetching external jobs", e);
        }
    }

    public boolean healthCheck() {
        try {
            restClient.get()
                    .uri("/jobs")
                    .retrieve()
                    .toBodilessEntity();

            log.debug("External API health check: OK");
            return true;

        } catch (Exception e) {
            log.warn("External API health check failed: {}", e.getMessage());
            return false;
        }
    }

}
