package io.github.vivianagh.jobberwocky.application.servicio;

import io.github.vivianagh.jobberwocky.domain.model.Job;
import io.github.vivianagh.jobberwocky.domain.model.JobSearchCriteria;
import io.github.vivianagh.jobberwocky.domain.port.JobSource;
import io.github.vivianagh.jobberwocky.infrastructure.repository.JobRepository;
import io.github.vivianagh.jobberwocky.infrastructure.web.dto.JobRequest;
import io.github.vivianagh.jobberwocky.infrastructure.web.dto.JobResponse;
import io.github.vivianagh.jobberwocky.infrastructure.web.dto.PageResponse;
import io.github.vivianagh.jobberwocky.infrastructure.web.dto.SearchParams;
import io.github.vivianagh.jobberwocky.infrastructure.web.mapper.JobMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final JobSource jobSource;

    @Transactional
    public Job create(Job job) {
        job.setSource("INTERNAL");
        return jobRepository.save(job);
    }

    @Transactional(readOnly = true)
    public List<Job> search(JobSearchCriteria criteria) {
        if (criteria == null) {
            criteria = JobSearchCriteria.builder().build();
        }
        criteria.validate();
        return jobSource.searchJobs(criteria);
    }

    @Transactional(readOnly = true)
    public PageResponse<Job> searchPaged(JobSearchCriteria criteria,
                                         Integer page,
                                         Integer size,
                                         String sort) {
        if (criteria == null) criteria = JobSearchCriteria.builder().build();
        criteria.validate();

        List<Job> all = jobSource.searchJobs(criteria);
        sortList(all, sort);

        int p = (page == null || page < 0) ? 0 : page;
        int s = (size == null || size <= 0 || size > 100) ? 20 : size;
        int from = Math.min(p * s, all.size());
        int to = Math.min(from + s, all.size());
        List<Job> slice = all.subList(from, to);

        return new PageResponse<>(slice, p, s, all.size());
    }

    private void sortList(List<Job> list, String sort) {
        if (sort == null || sort.isBlank()) return; // sin orden, tal cual viene

        String[] parts = sort.split(",", 2);
        String field = parts[0].trim().toLowerCase();
        boolean desc = parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim());

        Comparator<Job> cmp = switch (field) {
            case "title"     -> Comparator.comparing(Job::getTitle, Comparator.nullsLast(String::compareToIgnoreCase));
            case "company"   -> Comparator.comparing(Job::getCompany, Comparator.nullsLast(String::compareToIgnoreCase));
            case "country"   -> Comparator.comparing(Job::getCountry, Comparator.nullsLast(String::compareToIgnoreCase));
            case "salary"    -> Comparator.comparing(Job::getSalary, Comparator.nullsLast(BigDecimal::compareTo));
            case "createdat" -> Comparator.comparing(Job::getCreatedAt, Comparator.nullsLast(LocalDateTime::compareTo));
            default          -> null;
        };

        if (cmp != null) {
            if (desc) cmp = cmp.reversed();
            list.sort(cmp);
        }
    }





}
