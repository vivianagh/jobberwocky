package io.github.vivianagh.jobberwocky.infrastructure.adapter;

import io.github.vivianagh.jobberwocky.domain.model.Job;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExternalJobResponseAdapter {
    private final ObjectMapper objectMapper;
    private final XmlSkillsParser xmlSkillsParser;

    /**
     * Adapt messy external format to our Job domain model
     *
     * @param rawJson JSON string from external API
     * @return List of Job entities, empty if parsing fails
     */
    public List<Job> adapt(String rawJson) {
        // Handle null or empty input
        if (rawJson == null || rawJson.isBlank()) {
            log.debug("Raw JSON is null or blank");
            return List.of();
        }

        List<Job> jobs = new ArrayList<>();

        try {
            // Parse JSON: Map<Country, List<Array[title, salary, skillsXml]>>
            Map<String, List<List<Object>>> countryJobsMap = objectMapper.readValue(
                    rawJson,
                    new TypeReference<Map<String, List<List<Object>>>>() {}
            );

            // Transform each country's jobs
            for (Map.Entry<String, List<List<Object>>> entry : countryJobsMap.entrySet()) {
                String country = entry.getKey();
                List<List<Object>> jobArrays = entry.getValue();

                if (jobArrays == null) {
                    continue;
                }

                for (List<Object> jobArray : jobArrays) {
                    try {
                        Job job = transformJobArray(country, jobArray);
                        jobs.add(job);
                    } catch (Exception e) {
                        log.warn("Failed to parse job array from country {}: {}. Array: {}",
                                country, e.getMessage(), jobArray);
                        // Continue with other jobs - resilient parsing
                    }
                }
            }

            log.info("Successfully adapted {} jobs from external source", jobs.size());

        } catch (Exception e) {
            log.error("Failed to parse external API response: {}", e.getMessage());
            // Return empty list instead of failing entire search
        }

        return jobs;
    }

    /**
     * Transform single job array [title, salary, skillsXml] to Job entity
     *
     * @param country Country from the map key
     * @param jobArray Array with 3 elements: [title, salary, skillsXml]
     * @return Job entity
     * @throws IllegalArgumentException if array doesn't have 3 elements
     */
    private Job transformJobArray(String country, List<Object> jobArray) {
        if (jobArray == null || jobArray.size() != 3) {
            throw new IllegalArgumentException(
                    "Expected array of 3 elements, got: " +
                            (jobArray == null ? "null" : jobArray.size())
            );
        }

        // Extract array elements
        String title = jobArray.get(0).toString();
        BigDecimal salary = new BigDecimal(jobArray.get(1).toString());
        String skillsXml = jobArray.get(2).toString();

        // Parse skills XML
        Set<String> skills = xmlSkillsParser.parseSkills(skillsXml);

        // Generate external ID for deduplication
        String externalId = generateExternalId(country, title, salary);

        return Job.builder()
                .title(title)
                .company("External Company") // Not provided by API - use default
                .country(country)
                .salary(salary)
                .skills(skills)
                .source("EXTERNAL_API")
                .externalId(externalId)
                .description("Job from external source")
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Generate unique ID for external jobs (for deduplication)
     *
     * Format: EXT_COUNTRY_TITLE_SALARY
     * Example: EXT_USA_CLOUD_ENGINEER_65000
     *
     * @param country Job country
     * @param title Job title
     * @param salary Job salary
     * @return External ID string
     */
    private String generateExternalId(String country, String title, BigDecimal salary) {
        String normalizedTitle = title.replaceAll("\\s+", "_").toUpperCase();
        return String.format("EXT_%s_%s_%s",
                country.toUpperCase(),
                normalizedTitle,
                salary.toString()
        );
    }
}
