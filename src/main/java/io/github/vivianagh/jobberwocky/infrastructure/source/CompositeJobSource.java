package io.github.vivianagh.jobberwocky.infrastructure.source;

import io.github.vivianagh.jobberwocky.domain.model.Job;
import io.github.vivianagh.jobberwocky.domain.model.JobSearchCriteria;
import io.github.vivianagh.jobberwocky.domain.port.JobSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;


import java.math.BigDecimal;
import java.util.*;

/**
 * Add pattern Facade/Composite for multiples JobSource.
 * - Adding a new source does not change the controller (OCP)
 * - Keeps the controller depending on the port (JobSource) only (DIP)
 * - Dedup order: externalId -> id -> contentKey(title|country|salary)
 * - prefer INTERNAL over others when duplicates collide
 */
@Component
@Primary
@Slf4j
public class CompositeJobSource implements JobSource {

    private final List<JobSource> sources;

    public CompositeJobSource(List<JobSource> sources) {
        // Avoiding including ourselves
        this.sources = sources.stream()
                .filter(ds -> !(ds instanceof CompositeJobSource))
                .toList();
    }

    @Override
    public List<Job> searchJobs(JobSearchCriteria criteria) {
        if (criteria == null) return List.of();

        List<Job> aggregated = new ArrayList<>();

        for (JobSource src : sources) {
            if (!safeIsAvailable(src)) continue;
            try {
                List<Job> partial = src.searchJobs(criteria);
                if (partial != null && !partial.isEmpty()) {
                    aggregated.addAll(partial);
                }
            } catch (Exception e) {
                log.error("Error searching jobs", e);
            }
        }
        // 2) Deduplicate with a predictable, explainable policy
        //    Key priority: externalId -> id -> contentKey(title|country|salary)
        //     prefer INTERNAL over others
        Map<String, Job> unique = new LinkedHashMap<>();
        for (Job j : aggregated) {
            String key = dedupKey(j);
            Job current = unique.get(key);
            if (current == null) {
                unique.put(key, j);
            } else {
                // If duplicate, keep INTERNAL over non-INTERNAL
                if (isInternal(j) && !isInternal(current)) {
                    unique.put(key, j);
                }
            }
        }

        return new ArrayList<>(unique.values());
    }

    @Override
    public String getSourceName() {
        return "COMPOSITE";
    }

    @Override
    public boolean isAvailable() {
        // Composite is "available" if at least one source is available
        for (JobSource s : sources) {
            if (safeIsAvailable(s)) return true;
        }
        return false;
    }

    // -------- Helpers --------
    private boolean safeIsAvailable(JobSource s) {
        try {
            return s != null && s.isAvailable();
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean isInternal(Job j) {
        String src = j != null ? j.getSource() : null;
        return src != null && src.equalsIgnoreCase("INTERNAL");
    }

    /**
     * Build a stable dedup key using the following precedence:
     * 1) externalId if present (works across processes/DBs)
     * 2) id (DB PK, only valid for internal persisted jobs)
     * 3) contentKey(title|country|salary) as a fallback
     */
    private String dedupKey(Job j) {
        if (j == null) return "null";
        if (j.getExternalId() != null && !j.getExternalId().isBlank()) {
            return "EXT::" + j.getExternalId().trim();
        }
        if (j.getId() != null) {
            return "ID::" + j.getId();
        }
        return "CNT::" + contentKey(j.getTitle(), j.getCountry(), j.getSalary());
    }

    /**
     * Very basic content-based key:
     * - normalize title (lowercase alnum)
     * - lowercase country
     * - normalize salary to plain string
     *
     * This is a pragmatic fallback when neither externalId nor id exists.
     */
    private String contentKey(String title, String country, BigDecimal salary) {
        String t = normalizeTitle(title);
        String c = country == null ? "" : country.trim().toLowerCase(Locale.ROOT);
        String s = salary == null ? "" : salary.toPlainString();
        return t + "|" + c + "|" + s;
    }

    private String normalizeTitle(String title) {
        if (title == null) return "";
        // Keep letters/digits, lowercase, and collapse spaces
        String base = title.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9\\s]", " ");
        return base.trim().replaceAll("\\s+", " ");
    }
}
